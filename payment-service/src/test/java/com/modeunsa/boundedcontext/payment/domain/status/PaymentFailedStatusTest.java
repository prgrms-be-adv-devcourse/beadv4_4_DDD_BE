package com.modeunsa.boundedcontext.payment.domain.status;

import static org.assertj.core.api.Assertions.assertThat;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Payment 결제 실패 상태 테스트")
class PaymentFailedStatusTest {

  @Test
  @DisplayName("failedPayment 성공 - 결제 실패 시 FAILED 상태로 변경되고 실패 정보가 저장됨")
  void failedPaymentNonFinalFailure() {

    // given
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
    PaymentErrorCode errorCode = PaymentErrorCode.INVALID_PAYMENT;
    String message = "유효한 결제가 아닙니다.";

    // when
    payment.updateFailureInfo(errorCode, message);

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(payment.getFailedErrorCode()).isEqualTo(errorCode);
    assertThat(payment.getFailedReason()).isEqualTo(message);
    assertThat(payment.getFailedAt()).isNotNull();
  }

  @Test
  @DisplayName("failedPayment 성공 - 최종 결제 실패 시 FINAL_FAILED 상태로 변경되고 실패 정보가 저장됨")
  void failedPaymentFinalFailure() {

    // given
    PaymentId paymentId = PaymentId.create(2L, "ORDER67890");
    Payment payment =
        Payment.create(
            paymentId,
            2L,
            BigDecimal.valueOf(75000),
            LocalDateTime.now().plusDays(1),
            ProviderType.TOSS_PAYMENTS,
            PaymentPurpose.PRODUCT_PURCHASE);
    payment.changeToInProgress();
    PaymentErrorCode errorCode = PaymentErrorCode.OVERDUE_PAYMENT_DEADLINE;
    String message = "결제 기한이 초과되었습니다.";

    // when
    payment.updateFailureInfo(errorCode, message);

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FINAL_FAILED);
    assertThat(payment.getFailedErrorCode()).isEqualTo(errorCode);
    assertThat(payment.getFailedReason()).isEqualTo(message);
    assertThat(payment.getFailedAt()).isNotNull();
  }
}
