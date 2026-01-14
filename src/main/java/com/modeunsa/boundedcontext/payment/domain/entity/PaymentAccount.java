package com.modeunsa.boundedcontext.payment.domain.entity;

import static jakarta.persistence.CascadeType.PERSIST;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAccount extends GeneratedIdAndAuditedEntity {

  @Builder.Default
  @OneToMany(
      mappedBy = "paymentAccount",
      cascade = {PERSIST})
  private List<PaymentAccountLog> paymentAccountLogs = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false, unique = true)
  private PaymentMember member;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  public static PaymentAccount create(PaymentMember paymentMember) {
    return PaymentAccount.builder().member(paymentMember).balance(BigDecimal.ZERO).build();
  }

  public void credit(
      BigDecimal amount,
      PaymentEventType paymentEventType,
      Long relId,
      ReferenceType referenceType) {
    validateAmount(amount);
    BigDecimal balanceBefore = this.balance;
    this.balance = this.balance.add(amount);
    addPaymentAccountLog(
        amount, paymentEventType, balanceBefore, this.balance, relId, referenceType);
  }

  public void credit(BigDecimal amount, PaymentEventType paymentEventType) {
    credit(amount, paymentEventType, member.getId(), ReferenceType.PAYMENT_MEMBER);
  }

  public boolean canPayOrder(BigDecimal salePrice) {
    return this.balance.compareTo(salePrice) >= 0;
  }

  public void debit(
      BigDecimal amount,
      PaymentEventType paymentEventType,
      Long relId,
      ReferenceType referenceType) {
    validateAmount(amount);
    BigDecimal balanceBefore = this.balance;
    this.balance = this.balance.subtract(amount);
    addPaymentAccountLog(
        amount.negate(), paymentEventType, balanceBefore, this.balance, relId, referenceType);
  }

  public BigDecimal getShortFailAmount(BigDecimal pgPaymentAmount) {
    return pgPaymentAmount.subtract(this.balance);
  }

  private void addPaymentAccountLog(
      BigDecimal amount,
      PaymentEventType paymentEventType,
      BigDecimal balanceBefore,
      BigDecimal balanceAfter,
      Long relId,
      ReferenceType referenceType) {
    PaymentAccountLog paymentAccountLog =
        PaymentAccountLog.addAccountLog(
            this, amount, paymentEventType, balanceBefore, balanceAfter, relId, referenceType);
    this.paymentAccountLogs.add(paymentAccountLog);
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_INVALID);
    }
  }
}
