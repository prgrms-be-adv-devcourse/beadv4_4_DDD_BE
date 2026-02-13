package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import java.util.Optional;

public interface PaymentReader {
  Optional<Payment> findById(PaymentId paymentId);
}
