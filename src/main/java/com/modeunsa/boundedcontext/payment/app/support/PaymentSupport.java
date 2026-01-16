package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.out.PaymentRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSupport {

  private final PaymentRepository paymentRepository;

  public Payment getPaymentById(PaymentId paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_NOT_FOUND));
  }

  public void changePaymentStatus(Long buyerId, String orderNo, PaymentStatus paymentStatus) {
    PaymentId paymentId = new PaymentId(buyerId, orderNo);
    Payment payment = getPaymentById(paymentId);
    payment.changeStatus(paymentStatus);
  }
}
