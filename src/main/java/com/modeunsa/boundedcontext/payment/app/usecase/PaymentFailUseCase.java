package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentFailUseCase {

  private final PaymentSupport paymentSupport;

  public void execute(PaymentFailedEvent event) {
    PaymentId paymentId = PaymentId.create(event.memberId(), event.orderNo());
    Payment payment = paymentSupport.getPaymentById(paymentId);
    payment.failedPayment(event.failureReason(), event.memberId(), event.orderNo());
  }
}
