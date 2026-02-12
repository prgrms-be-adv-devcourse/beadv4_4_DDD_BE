package com.modeunsa.boundedcontext.payment.app.usecase.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.ApiApplication;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import com.modeunsa.boundedcontext.payment.out.PaymentStore;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = ApiApplication.class)
@ActiveProfiles("test")
@DisplayName("PaymentInitializeUseCase 테스트")
class PaymentInitializeUseCaseTest {

  @MockitoBean private PaymentSupport paymentSupport;
  @MockitoBean private PaymentStore paymentStore;

  @Autowired private PaymentInitializeUseCase paymentInitializeUseCase;

  private static PaymentRequest defaultPaymentRequest() {
    return PaymentRequest.builder()
        .orderId(1L)
        .orderNo("ORD-001")
        .totalAmount(BigDecimal.valueOf(10000))
        .paymentDeadlineAt(LocalDateTime.now().plusMinutes(10))
        .providerType(ProviderType.TOSS_PAYMENTS)
        .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
        .build();
  }

  @Test
  @DisplayName("일시적 DB 오류 시 재시도 후 성공 - store()가 3번 호출되고 최종 성공")
  void execute_retriesOnTransientFailure_thenSucceeds() {
    // given
    Long memberId = 1L;
    PaymentRequest request = defaultPaymentRequest();
    PaymentId paymentId = PaymentId.create(memberId, request.orderNo());

    when(paymentSupport.getOptPaymentById(paymentId)).thenReturn(Optional.empty());

    // 1·2차: 일시적 오류, 3차: 성공
    when(paymentStore.store(any(Payment.class)))
        .thenThrow(new DataAccessResourceFailureException("connection timeout"))
        .thenThrow(new DataAccessResourceFailureException("connection timeout"))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    PaymentProcessContext result = paymentInitializeUseCase.execute(memberId, request);

    // then: store() 3번 호출, 결과 정상
    verify(paymentStore, times(3)).store(any(Payment.class));
    assertThat(result.orderId()).isEqualTo(request.orderId());
    assertThat(result.orderNo()).isEqualTo(request.orderNo());
    assertThat(result.totalAmount()).isEqualByComparingTo(request.totalAmount());
  }

  @Test
  @DisplayName("복합키 중복(DataIntegrityViolation) 시 재시도 없이 1번만 호출 후 비즈니스 예외")
  void execute_doesNotRetryOnDataIntegrityViolation_throwsGeneralException() {
    // given
    Long memberId = 2L;
    PaymentRequest request = defaultPaymentRequest();
    PaymentId paymentId = PaymentId.create(memberId, request.orderNo());

    when(paymentSupport.getOptPaymentById(paymentId)).thenReturn(Optional.empty());
    when(paymentStore.store(any(Payment.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    // when & then: 재시도 없이 1번만 store 호출
    assertThatThrownBy(() -> paymentInitializeUseCase.execute(memberId, request))
        .isInstanceOf(GeneralException.class)
        .satisfies(
            ex ->
                assertThat(((GeneralException) ex).getErrorStatus())
                    .isEqualTo(ErrorStatus.PAYMENT_DUPLICATE));

    verify(paymentStore, times(1)).store(any(Payment.class));
  }
}
