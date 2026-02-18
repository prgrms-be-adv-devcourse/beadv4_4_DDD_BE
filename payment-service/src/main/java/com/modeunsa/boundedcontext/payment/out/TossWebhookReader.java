package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentTossWebhookLog;
import java.util.Optional;

public interface TossWebhookReader {

  PaymentTossWebhookLog findById(Long webhookLogId);

  Optional<PaymentTossWebhookLog> findByTransmissionId(String transmissionId);
}
