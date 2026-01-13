package com.modeunsa.boundedcontext.payment.domain.entity;

import static jakarta.persistence.CascadeType.PERSIST;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
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

  private long balance;

  public static PaymentAccount create(PaymentMember paymentMember) {
    return PaymentAccount.builder().member(paymentMember).balance(0L).build();
  }

  public void credit(
      long amount, PaymentEventType paymentEventType, Long relId, ReferenceType referenceType) {
    validateAmount(amount);
    long balanceBefore = this.balance;
    this.balance += amount;
    addPaymentAccountLog(
        amount, paymentEventType, balanceBefore, this.balance, relId, referenceType);
  }

  public void credit(long amount, PaymentEventType paymentEventType) {
    credit(amount, paymentEventType, member.getId(), ReferenceType.PAYMENT_MEMBER);
  }

  private void addPaymentAccountLog(
      long amount,
      PaymentEventType paymentEventType,
      long balanceBefore,
      long balanceAfter,
      Long relId,
      ReferenceType referenceType) {
    PaymentAccountLog paymentAccountLog =
        PaymentAccountLog.addAccountLog(
            this, amount, paymentEventType, balanceBefore, balanceAfter, relId, referenceType);
    this.paymentAccountLogs.add(paymentAccountLog);
  }

  private void validateAmount(long amount) {
    if (amount <= 0) {
      throw new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_INVALID);
    }
  }
}
