package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentInboxEvent;

public interface PaymentInboxStore {
  PaymentInboxEvent store(PaymentInboxEvent newPaymentInboxEvent);
}
