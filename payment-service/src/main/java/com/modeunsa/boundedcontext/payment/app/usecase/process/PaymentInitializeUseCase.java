package com.modeunsa.boundedcontext.payment.app.usecase.process;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_DUPLICATE;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.out.PaymentStore;
import com.modeunsa.global.aop.saga.OrderSagaStep;
import com.modeunsa.global.aop.saga.SagaStep;
import com.modeunsa.global.aop.saga.SagaType;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.retry.RetryOnDbFailure;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentInitializeUseCase {

  private final PaymentSupport paymentSupport;
  private final PaymentStore paymentStore;

  /*
   * 결제 초기화 : 기존 결제 건이 있으면 재시도, 없으면 신규 생성
   * 동시성 이슈를 대비해 복합키 중복 예외 처리 포함
   */
  @SagaStep(sagaName = SagaType.ORDER_FLOW, step = OrderSagaStep.PAYMENT_PENDING)
  @RetryOnDbFailure
  public PaymentProcessContext execute(Long memberId, PaymentRequest paymentRequest) {
    PaymentId paymentId = PaymentId.create(memberId, paymentRequest.orderNo());

    Optional<Payment> findPayment = paymentSupport.getOptPaymentById(paymentId);
    if (findPayment.isPresent()) {
      Payment payment = findPayment.get();
      payment.changeToPending(paymentRequest.paymentDeadlineAt());
      return PaymentProcessContext.fromPaymentForInitialize(payment);
    }

    Payment payment =
        Payment.create(
            paymentId,
            paymentRequest.orderId(),
            paymentRequest.totalAmount(),
            paymentRequest.paymentDeadlineAt(),
            paymentRequest.providerType(),
            paymentRequest.paymentPurpose());
    return savePayment(payment);
  }

  /*
   * 결제 정보 저장 / 동시성 이슈를 대비해 복합키 중복 예외 처리 포함
   * 잘못된 결제 요청에 대한 상태 임으로, client 로 예외 전파
   */
  private PaymentProcessContext savePayment(Payment payment) {

    try {
      // 복합키 저장을 위해 Payment 를 먼저 저장 후 로그를 추가
      Payment saved = paymentStore.store(payment);
      saved.addInitialPaymentLog(saved);
      return PaymentProcessContext.fromPaymentForInitialize(saved);
    } catch (DataIntegrityViolationException e) {
      throw new GeneralException(PAYMENT_DUPLICATE);
    }
  }
}
