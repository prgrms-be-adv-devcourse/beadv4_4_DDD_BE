package com.modeunsa.boundedcontext.payment.domain.entity;

import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.INVALID_CHARGE_AMOUNT;
import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.INVALID_PAYMENT;
import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.INVALID_PAYMENT_STATUS;
import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.OVERDUE_PAYMENT_DEADLINE;
import static jakarta.persistence.CascadeType.PERSIST;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
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
import java.util.Set;
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
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentPurpose paymentPurpose;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  private boolean needPgPayment;

  @Column(precision = 19, scale = 2)
  private BigDecimal requestPgAmount;

  @Column(nullable = false)
  private LocalDateTime paymentDeadlineAt;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private ProviderType paymentProvider;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentErrorCode failedErrorCode;

  private String failedReason;

  private LocalDateTime failedAt;

  @Column(precision = 19, scale = 2)
  private BigDecimal pgPaymentAmount;

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

  private static final Set<PaymentStatus> ALLOWED_FOR_IN_PROGRESS =
      Set.of(PaymentStatus.PENDING, PaymentStatus.FAILED);

  private static final Set<PaymentStatus> ALLOWED_FOR_SUCCESS =
      Set.of(PaymentStatus.IN_PROGRESS, PaymentStatus.APPROVED);

  private static final Set<PaymentStatus> FINAL_TERMINAL_STATUSES =
      Set.of(
          PaymentStatus.SUCCESS,
          PaymentStatus.FINAL_FAILED,
          PaymentStatus.CANCELED,
          PaymentStatus.REFUNDED);

  public static Payment create(
      PaymentId id,
      Long orderId,
      BigDecimal totalAmount,
      LocalDateTime paymentDeadlineAt,
      ProviderType providerType,
      PaymentPurpose paymentPurpose) {
    validateTotalAmount(totalAmount);
    return Payment.builder()
        .id(id)
        .orderId(orderId)
        .totalAmount(totalAmount)
        .paymentDeadlineAt(paymentDeadlineAt)
        .paymentProvider(providerType)
        .paymentPurpose(paymentPurpose)
        .status(PaymentStatus.PENDING)
        .build();
  }

  public void addInitialLog(Payment payment) {
    PaymentLog paymentLog = PaymentLog.addInitialLog(payment, PaymentStatus.PENDING);
    this.paymentLogs.add(paymentLog);
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

  public void approveTossPayment(TossPaymentsConfirmResponse tossRes) {
    validatePaymentStatus(PaymentStatus.IN_PROGRESS);
    this.pgOrderName = tossRes.orderName();
    this.pgMethod = tossRes.method();
    this.pgStatus = tossRes.status();
    this.pgStatusCode = HttpStatus.OK.value();
    this.pgPaymentAmount = BigDecimal.valueOf(tossRes.totalAmount());
    changeStatus(PaymentStatus.APPROVED);
  }

  public void failedPayment(PaymentErrorCode errorCode, String failureMessage) {
    this.failedErrorCode = errorCode;
    this.failedAt = LocalDateTime.now();
    this.failedReason = failureMessage;
    PaymentStatus failedStatus =
        errorCode.isFinalFailure() ? PaymentStatus.FINAL_FAILED : PaymentStatus.FAILED;
    changeStatusByFailure(failedStatus, failureMessage);
  }

  public void failedTossPayment(HttpStatus httpStatus, String message) {
    this.pgStatusCode = httpStatus.value();
    this.pgFailureReason = message;
    this.failedAt = LocalDateTime.now();
    changeStatusByFailure(PaymentStatus.FAILED, message);
  }

  public void initPayment(LocalDateTime paymentDeadlineAt) {
    if (!isRetryable()) {
      throw new PaymentDomainException(
          INVALID_PAYMENT,
          String.format(
              "초기화 가능한 상태가 아닙니다. 회원 ID: %d, 주문 번호: %s, 현재 상태: %s",
              getId().getMemberId(), getId().getOrderNo(), this.status));
    }
    this.paymentDeadlineAt = paymentDeadlineAt;
    changeStatus(PaymentStatus.PENDING);
  }

  public void changeInProgress() {
    validateNotTerminalStatus();
    validateCanChangeToInProgress();
    changeStatus(PaymentStatus.IN_PROGRESS);
  }

  public void validatePgProcess() {
    validatePaymentStatus(PaymentStatus.IN_PROGRESS);
    validatePaymentDeadline();
  }

  public void changeSuccess() {
    validatePaymentStatusContains(ALLOWED_FOR_SUCCESS, PaymentStatus.SUCCESS);
    changeStatus(PaymentStatus.SUCCESS);
  }

  public void updatePgRequestInfo(boolean needPgPayment, BigDecimal requestPgAmount) {
    this.needPgPayment = needPgPayment;
    this.requestPgAmount = requestPgAmount;
  }

  public void validateChargeAmount(BigDecimal chargeAmount) {
    if (chargeAmount == null) {
      throw new PaymentDomainException(
          INVALID_CHARGE_AMOUNT,
          String.format(
              "PG 결제 금액이 null 입니다. 회원 ID: %d, 주문 번호: %s, 요청 PG 금액: %s",
              getId().getMemberId(), getId().getOrderNo(), this.requestPgAmount));
    }

    if (requestPgAmount.compareTo(chargeAmount) != 0) {
      throw new PaymentDomainException(
          INVALID_CHARGE_AMOUNT,
          String.format(
              "부족한 금액과 PG 결제 금액이 다릅니다. 회원 ID: %d, 주문 번호: %s, 부족 금액: %s, PG 요청 금액: %s",
              getId().getMemberId(), getId().getOrderNo(), this.requestPgAmount, chargeAmount));
    }
  }

  public void updatePgInfo(PaymentProcessContext context) {
    this.pgPaymentKey = context.paymentKey();
    this.pgCustomerName = context.pgCustomerName();
    this.pgCustomerEmail = context.pgCustomerEmail();
    this.pgOrderId = context.pgOrderId();
  }

  public void syncToInProgress() {
    validateNotTerminalStatus();
    if (this.status == PaymentStatus.PENDING) {
      changeInProgress();
    }
  }

  public void syncToApproved() {
    validateNotTerminalStatus();
    if (this.status == PaymentStatus.IN_PROGRESS) {
      changeStatus(PaymentStatus.APPROVED);
    }
  }

  public void syncToCanceled() {
    validateNotTerminalStatus();
    changeStatus(PaymentStatus.CANCELED);
  }

  private boolean isTerminalStatus() {
    return FINAL_TERMINAL_STATUSES.contains(this.status);
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
          INVALID_PAYMENT, String.format("주문금액은 0원 이상이어야 합니다. 요청 금액: %s", totalAmount));
    }
  }

  private boolean isRetryable() {
    return !isTerminalStatus()
        && (this.status == PaymentStatus.PENDING || this.status == PaymentStatus.FAILED);
  }

  private void validateCanChangeToInProgress() {
    validatePaymentStatusContains(ALLOWED_FOR_IN_PROGRESS, PaymentStatus.IN_PROGRESS);
    validatePaymentDeadline();
  }

  private void validatePaymentStatus(PaymentStatus paymentStatus) {
    if (this.status != paymentStatus) {
      throw new PaymentDomainException(
          INVALID_PAYMENT_STATUS,
          String.format(
              "현재 결제 상태는 변경할 수 없는 상태입니다. 회원 ID: %d, 주문 번호: %s, 현재 상태: %s, 필요한 결제 상태: %s",
              getId().getMemberId(), getId().getOrderNo(), this.status, paymentStatus));
    }
  }

  private void validatePaymentStatusContains(
      Set<PaymentStatus> allowStatus, PaymentStatus paymentStatus) {
    if (!allowStatus.contains(this.status)) {
      throw new PaymentDomainException(
          INVALID_PAYMENT_STATUS,
          String.format(
              "현재 상태는 변경할 수 없는 상태입니다. 회원 ID: %d, 주문 번호: %s, 현재 상태: %s, 필요한 상태: %s",
              getId().getMemberId(), getId().getOrderNo(), this.status, paymentStatus));
    }
  }

  private void validatePaymentDeadline() {
    if (this.paymentDeadlineAt.isBefore(LocalDateTime.now())) {
      throw new PaymentDomainException(
          OVERDUE_PAYMENT_DEADLINE,
          String.format(
              "결제 유효기간이 만료되어 결제 진행상태로 변경할 수 없습니다. 회원 ID: %d, 주문 번호: %s, 결제 마감일: %s",
              getId().getMemberId(), getId().getOrderNo(), this.paymentDeadlineAt));
    }
  }

  private void validateNotTerminalStatus() {
    if (isTerminalStatus()) {
      throw new PaymentDomainException(
          INVALID_PAYMENT,
          String.format(
              "최종 상태의 결제 건은 변경할 수 없습니다. 더 이상 상태 변경이 불가능합니다. 회원 ID: %d, 주문 번호: %s, 현재 상태: %s",
              getId().getMemberId(), getId().getOrderNo(), this.status));
    }
  }
}
