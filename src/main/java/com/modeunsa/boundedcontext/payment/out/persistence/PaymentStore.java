package com.modeunsa.boundedcontext.payment.out.persistence;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;

public interface PaymentStore {
  Payment store(Payment newPayment);
}
