package com.modeunsa.boundedcontext.payment.app.usecase.process;

import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.global.aop.saga.OrderSagaStep;
import com.modeunsa.global.aop.saga.SagaStep;
import com.modeunsa.global.aop.saga.SagaType;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import com.modeunsa.shared.payment.event.PaymentFinalFailureEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentFailureUseCase {

  private final EventPublisher eventPublisher;
  private final PaymentSupport paymentSupport;

  public void execute(PaymentFailedEvent event) {
    PaymentErrorCode error = PaymentErrorCode.fromCode(event.errorCode());

    if (error.isFinalFailure()) {
      handlingFinalFailedEvent(event, error);
    } else {
      handlingFailedEvent(event, error);
    }
  }

  @SagaStep(sagaName = SagaType.ORDER_FLOW, step = OrderSagaStep.PAYMENT_FINAL_FAILED)
  private void handlingFinalFailedEvent(PaymentFailedEvent event, PaymentErrorCode error) {
    recordFailure(event, error);
    eventPublisher.publish(
        new PaymentFinalFailureEvent(
            new PaymentDto(event.orderId(), event.orderNo(), event.memberId(), event.amount())));
  }

  @SagaStep(sagaName = SagaType.ORDER_FLOW, step = OrderSagaStep.PAYMENT_FAILED)
  private void handlingFailedEvent(PaymentFailedEvent event, PaymentErrorCode error) {
    recordFailure(event, error);
  }

  private void recordFailure(PaymentFailedEvent event, PaymentErrorCode error) {
    PaymentId paymentId = PaymentId.create(event.memberId(), event.orderNo());
    Payment payment = paymentSupport.getPaymentById(paymentId);
    payment.updateFailureInfo(error, event.failureMessage());
  }
}
