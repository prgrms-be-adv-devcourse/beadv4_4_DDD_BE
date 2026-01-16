package com.modeunsa.boundedcontext.payment.domain.entity;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_INVALID;
import static jakarta.persistence.CascadeType.PERSIST;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payment_payment",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_payment_member_order",
          columnNames = {"member_id", "order_no"})
    })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends AuditedEntity {

  @EmbeddedId private PaymentId id;

  /*
   * 로그데이터의 경우 fetch, orphanRemoval 설정을 명시한다.
   */
  @Builder.Default
  @OneToMany(
      mappedBy = "payment",
      cascade = {PERSIST},
      fetch = FetchType.LAZY,
      orphanRemoval = false)
  private List<PaymentLog> paymentLogs = new ArrayList<>();

  @Builder.Default
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus status = PaymentStatus.READY;

  @NotNull private Long orderId;

  @NotNull @Positive private BigDecimal totalAmount;

  private BigDecimal pgPaymentAmount;

  private String pgProvider;

  private String pgCustomerKey;

  private String pgOrderNo;

  private String pgCustomerName;

  private String pgCustomerEmail;

  @Lob private Integer pgRawResponse;

  @Lob private String pgFailureReason;

  public static Payment create(
      PaymentId id, Long orderId, BigDecimal totalAmount, BigDecimal pgPaymentAmount) {
    validateTotalAmount(totalAmount);

    Payment payment =
        Payment.builder()
            .id(id)
            .orderId(orderId)
            .totalAmount(totalAmount)
            .pgPaymentAmount(pgPaymentAmount)
            .status(PaymentStatus.READY)
            .build();

    payment.addInitialLog();
    return payment;
  }

  public void changeStatus(PaymentStatus newStatus) {
    PaymentStatus beforeStatus = this.status;
    this.status = newStatus;
    addPaymentLog(beforeStatus, newStatus);
  }

  private void addInitialLog() {
    PaymentLog paymentLog = PaymentLog.addInitialLog(this, status);
    this.paymentLogs.add(paymentLog);
  }

  private void addPaymentLog(PaymentStatus beforeStatus, PaymentStatus afterStatus) {
    PaymentLog paymentLog = PaymentLog.addLog(this, beforeStatus, afterStatus);
    this.paymentLogs.add(paymentLog);
  }

  private static void validateTotalAmount(BigDecimal totalAmount) {
    if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new GeneralException(PAYMENT_INVALID);
    }
  }
}
