package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentTossWebhookLog;

public interface TossWebhookStore {
  PaymentTossWebhookLog store(PaymentTossWebhookLog newWebhookLog);
}
