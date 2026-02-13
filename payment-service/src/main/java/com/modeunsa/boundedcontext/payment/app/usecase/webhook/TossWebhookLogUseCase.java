package com.modeunsa.boundedcontext.payment.app.usecase.webhook;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentTossWebhookLog;
import com.modeunsa.boundedcontext.payment.out.TossWebhookStore;
import com.modeunsa.global.json.JsonConverter;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TossWebhookLogUseCase {

  private final JsonConverter jsonConverter;
  private final TossWebhookStore tossWebhookStore;

  public void execute(
      String transmissionId,
      OffsetDateTime transmissionTime,
      int retryCount,
      @Valid TossWebhookRequest request) {

    String payload = jsonConverter.serialize(request);

    PaymentTossWebhookLog webhookLog =
        PaymentTossWebhookLog.create(
            transmissionId, transmissionTime, retryCount, request.eventType(), payload);

    tossWebhookStore.store(webhookLog);
  }
}
