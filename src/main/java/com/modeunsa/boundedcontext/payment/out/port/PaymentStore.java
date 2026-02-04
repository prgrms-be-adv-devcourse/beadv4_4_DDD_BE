package com.modeunsa.boundedcontext.payment.out.port;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;

public interface PaymentStore {
  Payment store(Payment newPayment);
}
