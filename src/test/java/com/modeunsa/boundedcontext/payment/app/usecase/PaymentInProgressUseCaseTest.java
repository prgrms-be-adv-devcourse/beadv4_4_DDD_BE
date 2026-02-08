package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.app.usecase.process.PaymentInProgressUseCase;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import com.modeunsa.global.eventpublisher.EventPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentInProgressUseCase 테스트")
class PaymentInProgressUseCaseTest {

  @Mock private PaymentSupport paymentSupport;
  @Mock private PaymentMemberSupport paymentMemberSupport;
  @Mock private PaymentAccountSupport paymentAccountSupport;
  @Mock private EventPublisher eventPublisher;

  @InjectMocks private PaymentInProgressUseCase paymentInProgressUseCase;

  @Test
  @DisplayName("TOSS_PAYMENTS일 때 needPgPayment true, requestPgAmount가 totalAmount와 같음")
  void executeForPaymentRequest() {
    // given: TOSS_PAYMENTS 방식 결제 요청 context
    Long buyerId = 1L;
    String orderNo = "ORD-19800";
    Long orderId = 1000L;
    BigDecimal totalAmount = BigDecimal.valueOf(19800);
    LocalDateTime futureDeadline = LocalDateTime.now().plusMinutes(10);

    PaymentProcessContext context =
        PaymentProcessContext.builder()
            .buyerId(buyerId)
            .orderNo(orderNo)
            .orderId(orderId)
            .totalAmount(totalAmount)
            .needsPgPayment(false)
            .providerType(ProviderType.TOSS_PAYMENTS)
            .build();

    PaymentId paymentId = PaymentId.create(buyerId, orderNo);
    Payment payment =
        Payment.create(
            paymentId,
            orderId,
            totalAmount,
            futureDeadline,
            ProviderType.TOSS_PAYMENTS,
            PaymentPurpose.PRODUCT_PURCHASE);

    PaymentMember buyer =
        PaymentMember.create(buyerId, "buyer@example.com", "구매자", MemberStatus.ACTIVE);

    when(paymentSupport.getPaymentById(paymentId)).thenReturn(payment);
    when(paymentMemberSupport.getPaymentMemberById(buyerId)).thenReturn(buyer);

    // when
    PaymentProcessContext result = paymentInProgressUseCase.executeForPaymentRequest(context);

    // then: TOSS_PAYMENTS는 전액 PG 결제이므로 needPgPayment true, requestPgAmount == totalAmount
    assertThat(result.needsPgPayment()).isTrue();
    assertThat(result.requestPgAmount()).isEqualByComparingTo(totalAmount);
    assertThat(result.requestPgAmount()).isEqualByComparingTo(BigDecimal.valueOf(19800));

    // TOSS_PAYMENTS 분기에서는 결제 계좌 조회(loadBuyerAccount)를 호출하지 않음
    verify(paymentSupport).getPaymentById(paymentId);
    verify(paymentMemberSupport).getPaymentMemberById(buyerId);
    verify(paymentAccountSupport, never()).getPaymentAccountByMemberId(anyLong());
  }
}
