package com.modeunsa.boundedcontext.payment.domain.status;

import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.INVALID_PAYMENT_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
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
import org.springframework.http.HttpStatus;

@DisplayName("Payment 결제 승인 상태 테스트")
class PaymentApprovedStatusTest {

  @Test
  @DisplayName("approveTossPayment 성공 - IN_PROGRESS 상태에서 PG 정보 반영 후 APPROVED 상태로 변경")
  void approveTossPaymentSuccess() {

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

    String orderName = "주문_테스트";
    String method = "CARD";
    String status = "DONE";
    long totalAmount = 50_000L;
    TossPaymentsConfirmResponse tossRes =
        new TossPaymentsConfirmResponse(
            "payment_key_xxx", "ORDER_123456", orderName, method, totalAmount, status);

    // when
    payment.changeToApprove(tossRes);

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    assertThat(payment.getPgOrderName()).isEqualTo(orderName);
    assertThat(payment.getPgMethod()).isEqualTo(method);
    assertThat(payment.getPgStatus()).isEqualTo(status);
    assertThat(payment.getPgStatusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(payment.getPgPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(totalAmount));
  }

  @Test
  @DisplayName("approveTossPayment 실패 - IN_PROGRESS 가 아닌 경우")
  void approveTossPaymentFailureWhenNotInProgress() {

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

    TossPaymentsConfirmResponse tossRes =
        new TossPaymentsConfirmResponse(
            "payment_key_yyy", "ORDER_678901", "주문_테스트2", "TRANSFER", 75_000L, "DONE");

    assertThatThrownBy(() -> payment.changeToApprove(tossRes))
        .isInstanceOf(PaymentDomainException.class)
        .satisfies(
            ex -> {
              PaymentDomainException e = (PaymentDomainException) ex;
              assertThat(e.getErrorCode()).isEqualTo(INVALID_PAYMENT_STATUS);
            });
  }
}
