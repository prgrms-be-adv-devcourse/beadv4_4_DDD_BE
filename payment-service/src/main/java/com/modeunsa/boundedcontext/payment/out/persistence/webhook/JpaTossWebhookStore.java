package com.modeunsa.boundedcontext.payment.out.persistence.webhook;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentTossWebhookLog;
import com.modeunsa.boundedcontext.payment.out.TossWebhookStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaTossWebhookStore implements TossWebhookStore {

  private final TossWebhookRepository tossWebhookRepository;

  @Override
  public PaymentTossWebhookLog store(PaymentTossWebhookLog newWebhookLog) {
    return tossWebhookRepository.save(newWebhookLog);
  }
}
