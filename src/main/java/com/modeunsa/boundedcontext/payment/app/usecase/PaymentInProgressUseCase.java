package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentInProgressUseCase {

  private final PaymentSupport paymentSupport;

  public void execute(String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {
    PaymentId paymentId = PaymentId.create(confirmPaymentRequest.memberId(), orderNo);
    Payment payment = paymentSupport.getPaymentById(paymentId);
    payment.inProgressPayment(confirmPaymentRequest);
  }
}
