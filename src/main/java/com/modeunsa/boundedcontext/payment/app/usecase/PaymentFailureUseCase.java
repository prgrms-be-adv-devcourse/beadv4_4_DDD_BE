package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.payment.dto.PaymentDto;
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
    PaymentId paymentId = PaymentId.create(event.memberId(), event.orderNo());
    Payment payment = paymentSupport.getPaymentById(paymentId);
    payment.failedPayment(
        event.errorCode(), event.failureMessage(), event.memberId(), event.orderNo());

    PaymentErrorCode error = event.errorCode();
    if (error.isFinalFailure()) {
      publishFinalFailureEvent(event);
    }
  }

  private void publishFinalFailureEvent(PaymentFailedEvent event) {
    eventPublisher.publish(
        new PaymentFinalFailureEvent(
            new PaymentDto(event.orderId(), event.orderNo(), event.memberId(), event.amount())));
  }
}
