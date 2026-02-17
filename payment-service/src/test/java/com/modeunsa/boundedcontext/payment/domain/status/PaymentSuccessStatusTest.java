package com.modeunsa.boundedcontext.payment.domain.status;

import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.INVALID_PAYMENT_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Payment 결제 성공 상태 테스트")
class PaymentSuccessStatusTest {

  @Test
  @DisplayName("changeToSuccess 성공 - MODEUNSA_PAY이고 IN_PROGRESS 상태인 경우")
  void changeToSuccessModeunsaPayInProgress() {

    PaymentId paymentId = PaymentId.create(1L, "ORDER12345");
    Payment payment =
        Payment.create(
            paymentId,
            1L,
            BigDecimal.valueOf(50000),
            LocalDateTime.now().plusDays(1),
            ProviderType.MODEUNSA_PAY,
            PaymentPurpose.PRODUCT_PURCHASE);

    payment.changeToInProgress();

    // when
    payment.changeToSuccess();

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
  }

  @Test
  @DisplayName("changeToSuccess 실패 - MODEUNSA_PAY이고 IN_PROGRESS 상태가 아닌 경우")
  void changeToSuccessFailureWhenNotInProgress() {

    PaymentId paymentId = PaymentId.create(1L, "ORDER12345");
    LocalDateTime futureDeadline = LocalDateTime.now().plusDays(1);

    Payment payment =
        Payment.builder()
            .id(paymentId)
            .orderId(1L)
            .totalAmount(BigDecimal.valueOf(50000))
            .paymentDeadlineAt(futureDeadline)
            .paymentProvider(ProviderType.MODEUNSA_PAY)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
            .status(PaymentStatus.PENDING)
            .build();

    // when, then
    assertThatThrownBy(payment::changeToSuccess)
        .isInstanceOf(PaymentDomainException.class)
        .satisfies(
            exception -> {
              PaymentDomainException ex = (PaymentDomainException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(INVALID_PAYMENT_STATUS);
            });
  }

  @Test
  @DisplayName("changeToSuccess 성공 - TOSS PG 결제이면서 APPROVED 상태인 경우")
  void changeToSuccessTossPayInProgress() {

    PaymentId paymentId = PaymentId.create(1L, "ORDER12345");
    LocalDateTime futureDeadline = LocalDateTime.now().plusDays(1);

    Payment payment =
        Payment.builder()
            .id(paymentId)
            .orderId(1L)
            .totalAmount(BigDecimal.valueOf(50000))
            .paymentDeadlineAt(futureDeadline)
            .paymentProvider(ProviderType.TOSS_PAYMENTS)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
            .status(PaymentStatus.APPROVED)
            .build();

    // when
    payment.changeToSuccess();

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
  }

  @Test
  @DisplayName("changeToSuccess 성공 - TOSS PG 결제이면서 APPROVED 상태인 경우")
  void changeToSuccessFailureTossPayInProgress() {

    PaymentId paymentId = PaymentId.create(1L, "ORDER12345");
    LocalDateTime futureDeadline = LocalDateTime.now().plusDays(1);

    Payment payment =
        Payment.builder()
            .id(paymentId)
            .orderId(1L)
            .totalAmount(BigDecimal.valueOf(50000))
            .paymentDeadlineAt(futureDeadline)
            .paymentProvider(ProviderType.TOSS_PAYMENTS)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
            .status(PaymentStatus.FINAL_FAILED)
            .build();

    // when, then
    assertThatThrownBy(payment::changeToSuccess)
        .isInstanceOf(PaymentDomainException.class)
        .satisfies(
            exception -> {
              PaymentDomainException ex = (PaymentDomainException) exception;
              assertThat(ex.getErrorCode()).isEqualTo(INVALID_PAYMENT_STATUS);
            });
  }
}
