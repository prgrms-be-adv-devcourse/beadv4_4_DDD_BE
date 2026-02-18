package com.modeunsa.boundedcontext.payment.out.persistence.webhook;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentTossWebhookLog;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookErrorCode;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookException;
import com.modeunsa.boundedcontext.payment.out.TossWebhookReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaTossWebhookReader implements TossWebhookReader {

  private final TossWebhookRepository tossWebhookRepository;

  @Override
  public PaymentTossWebhookLog findById(Long webhookLogId) {
    return tossWebhookRepository
        .findById(webhookLogId)
        .orElseThrow(() -> new TossWebhookException(TossWebhookErrorCode.NOT_FOUND_WEBHOOK_EVENT));
  }

  @Override
  public Optional<PaymentTossWebhookLog> findByTransmissionId(String transmissionId) {
    return tossWebhookRepository.findByTransmissionId(transmissionId);
  }
}
