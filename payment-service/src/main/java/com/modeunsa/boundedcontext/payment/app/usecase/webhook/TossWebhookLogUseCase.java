package com.modeunsa.boundedcontext.payment.app.usecase.webhook;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentTossWebhookLog;
import com.modeunsa.boundedcontext.payment.out.TossWebhookReader;
import com.modeunsa.boundedcontext.payment.out.TossWebhookStore;
import com.modeunsa.global.json.JsonConverter;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TossWebhookLogUseCase {

  private final JsonConverter jsonConverter;
  private final TossWebhookReader tossWebhookReader;
  private final TossWebhookStore tossWebhookStore;

  public Long save(
      String transmissionId,
      OffsetDateTime transmissionTime,
      int retryCount,
      @Valid TossWebhookRequest request,
      String rawBody) {

    String payload = jsonConverter.serialize(request);

    Optional<PaymentTossWebhookLog> findWebhookLog =
        tossWebhookReader.findByTransmissionId(transmissionId);
    if (findWebhookLog.isPresent()) {
      PaymentTossWebhookLog webhookLog = findWebhookLog.get();
      webhookLog.update(retryCount, transmissionTime, payload);
      return webhookLog.getId();
    }

    PaymentTossWebhookLog webhookLog =
        PaymentTossWebhookLog.create(
            transmissionId,
            transmissionTime,
            retryCount,
            request.eventType(),
            request.data().orderId(),
            payload,
            rawBody);

    return tossWebhookStore.store(webhookLog).getId();
  }

  public void markAsSuccess(Long webhookLogId) {
    PaymentTossWebhookLog webhookLog = tossWebhookReader.findById(webhookLogId);
    webhookLog.markSuccess();
  }

  public void markAsFailed(Long webhookLogId, String message) {
    PaymentTossWebhookLog webhookLog = tossWebhookReader.findById(webhookLogId);
    webhookLog.markFailed(message);
  }
}
