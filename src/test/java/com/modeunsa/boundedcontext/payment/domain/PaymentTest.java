package com.modeunsa.boundedcontext.payment.domain;

import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.INVALID_PAYMENT_STATUS;
import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.OVERDUE_PAYMENT_DEADLINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Payment 도메인 테스트")
class PaymentTest {

  @Test
  @DisplayName("IN_PROGRESS 상태 변경 성공 - PENDING 상태이고 결제 마감일이 미래인 경우")
  void changeInProgressSuccess() {
    // given
    Long memberId = 1L;
    String orderNo = "ORDER12345";
    Long orderId = 1L;
    BigDecimal totalAmount = BigDecimal.valueOf(50000);
    LocalDateTime futureDeadline = LocalDateTime.now().plusDays(1); // 미래 날짜

    PaymentId paymentId = PaymentId.create(memberId, orderNo);
    Payment payment = Payment.create(paymentId, orderId, totalAmount, futureDeadline);

    // when
    payment.changeInProgress();

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
  }

  @Test
  @DisplayName("IN_PROGRESS 상태 변경 실패 - PENDING 상태가 아닌 경우")
  void changeInProgressFailureWhenStatusIsNotPending() {
    // given
    Long memberId = 1L;
    String orderNo = "ORDER12345";
    Long orderId = 1L;
    BigDecimal totalAmount = BigDecimal.valueOf(50000);
    LocalDateTime futureDeadline = LocalDateTime.now().plusDays(1);

    PaymentId paymentId = PaymentId.create(memberId, orderNo);
    Payment payment =
        Payment.builder()
            .id(paymentId)
            .orderId(orderId)
            .totalAmount(totalAmount)
            .paymentDeadlineAt(futureDeadline)
            .status(PaymentStatus.APPROVED)
            .build();

    // when, then
    assertThatThrownBy(payment::changeInProgress)
        .isInstanceOf(PaymentDomainException.class)
        .satisfies(
            exception -> {
              PaymentDomainException ex = (PaymentDomainException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(INVALID_PAYMENT_STATUS);
            });
  }

  @Test
  @DisplayName("IN_PROGRESS 상태 변경 실패 - 결제 마감일이 지난 경우")
  void changeInProgressFailureWhenDeadlinePassed() {
    // given
    Long memberId = 1L;
    String orderNo = "ORDER12345";
    Long orderId = 1L;
    BigDecimal totalAmount = BigDecimal.valueOf(50000);
    LocalDateTime pastDeadline = LocalDateTime.now().minusDays(1); // 과거 날짜

    PaymentId paymentId = PaymentId.create(memberId, orderNo);
    Payment payment = Payment.create(paymentId, orderId, totalAmount, pastDeadline);

    // when, then
    assertThatThrownBy(payment::changeInProgress)
        .isInstanceOf(PaymentDomainException.class)
        .satisfies(
            exception -> {
              PaymentDomainException ex = (PaymentDomainException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(OVERDUE_PAYMENT_DEADLINE);
            });
  }
}
