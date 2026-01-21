package com.modeunsa.boundedcontext.payment.domain.entity;

import static jakarta.persistence.CascadeType.PERSIST;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

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

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentErrorCode failedErrorCode;

  private LocalDateTime failedAt;

  @Column(precision = 19, scale = 2)
  private BigDecimal pgPaymentAmount;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private ProviderType pgProvider;

  @Convert(converter = EncryptedStringConverter.class)
  private String pgPaymentKey;

  private String pgOrderId;

  private String pgOrderName;

  private String pgStatus;

  private String pgMethod;

  @Convert(converter = EncryptedStringConverter.class)
  private String pgCustomerName;

  @Convert(converter = EncryptedStringConverter.class)
  private String pgCustomerEmail;

  private Integer pgStatusCode;

  @Lob private String pgFailureReason;

  public static Payment create(
      PaymentId id, Long orderId, BigDecimal totalAmount, BigDecimal pgPaymentAmount) {
    validateTotalAmount(totalAmount);
    return Payment.builder()
        .id(id)
        .orderId(orderId)
        .totalAmount(totalAmount)
        .pgPaymentAmount(pgPaymentAmount)
        .status(PaymentStatus.READY)
        .build();
  }

  public void changeStatus(PaymentStatus newStatus) {
    PaymentStatus beforeStatus = this.status;
    this.status = newStatus;
    addPaymentLog(beforeStatus, newStatus);
  }

  public void changeStatusByFailure(PaymentStatus newStatus, String message) {
    PaymentStatus before = this.status;
    this.status = newStatus;
    addPaymentLog(before, newStatus, message);
  }

  public void addInitialLog(Payment payment) {
    PaymentLog paymentLog = PaymentLog.addInitialLog(payment, PaymentStatus.READY);
    this.paymentLogs.add(paymentLog);
  }

  private void addPaymentLog(PaymentStatus beforeStatus, PaymentStatus afterStatus) {
    PaymentLog paymentLog = PaymentLog.addLog(this, beforeStatus, afterStatus);
    this.paymentLogs.add(paymentLog);
  }

  private void addPaymentLog(PaymentStatus beforeStatus, PaymentStatus afterStatus, String reason) {
    PaymentLog paymentLog = PaymentLog.addLog(this, beforeStatus, afterStatus, reason);
    this.paymentLogs.add(paymentLog);
  }

  private static void validateTotalAmount(BigDecimal totalAmount) {
    if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new PaymentDomainException(
          PaymentErrorCode.INSUFFICIENT_BALANCE,
          String.format("결제 금액은 0보다 작을 수 없습니다. 입력된 금액: %s", totalAmount));
    }
  }

  public void inProgressPayment(ConfirmPaymentRequest req) {
    this.pgCustomerName = req.pgCustomerName();
    this.pgCustomerEmail = req.pgCustomerEmail();
    this.pgPaymentKey = req.paymentKey();
    this.pgOrderId = req.orderId();
    this.pgPaymentAmount = BigDecimal.valueOf(req.amount());
    this.pgProvider = ProviderType.TOSS_PAYMENTS;
    changeStatus(PaymentStatus.IN_PROGRESS);
  }

  public void failedPayment(PaymentErrorCode errorCode, Long memberId, String orderNo) {
    this.failedErrorCode = errorCode;
    this.failedAt = LocalDateTime.now();
    changeStatusByFailure(PaymentStatus.FAILED, errorCode.format(memberId, orderNo));
  }

  public void approveTossPayment(TossPaymentsConfirmResponse tossRes) {
    this.pgOrderName = tossRes.orderName();
    this.pgMethod = tossRes.method();
    this.pgStatus = tossRes.status();
    this.pgStatusCode = HttpStatus.OK.value();
    changeStatus(PaymentStatus.APPROVED);
  }

  public void failedTossPayment(HttpStatus httpStatus, String message) {
    this.pgStatusCode = httpStatus.value();
    this.pgFailureReason = message;
    this.failedAt = LocalDateTime.now();
    changeStatusByFailure(PaymentStatus.FAILED, message);
  }
}
