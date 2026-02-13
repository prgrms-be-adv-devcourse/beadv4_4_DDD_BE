package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.out.PaymentReader;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSupport {

  private final PaymentReader paymentReader;

  public Payment getPaymentById(PaymentId paymentId) {
    return paymentReader
        .findById(paymentId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_NOT_FOUND));
  }

  public Optional<Payment> getOptPaymentById(PaymentId paymentId) {
    return paymentReader.findById(paymentId);
  }
}
