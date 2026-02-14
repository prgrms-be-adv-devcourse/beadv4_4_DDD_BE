package com.modeunsa.boundedcontext.payment.app.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentOutboxStatus;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxReader;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxStore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "outbox.poller.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentOutboxPoller {

  private final PaymentOutboxReader paymentOutboxReader;
  private final PaymentOutboxStore paymentOutboxStore;
  private final KafkaTemplate<String, String> kafkaTemplate;

  @Value("${outbox.poller.batch-size:100}")
  private int batchSize;

  @Value("${outbox.poller.max-retry:5}")
  private int maxRetry;

  @Scheduled(fixedDelayString = "${outbox.poller.interval-ms:5000}")
  public void pollAndPublish() {
    List<PaymentOutboxEvent> pendingEvents =
        paymentOutboxReader.findOutboxEventPage(
            PaymentOutboxStatus.PENDING, PageRequest.of(0, batchSize));

    if (pendingEvents.isEmpty()) {
      return;
    }

    log.debug("Polled {} outbox events", pendingEvents.size());

    for (PaymentOutboxEvent event : pendingEvents) {
      processEvent(event);
    }
  }

  private void processEvent(PaymentOutboxEvent event) {
    try {
      event.markAsProcessing();
      kafkaTemplate
          .send(event.getTopic(), event.getAggregateId(), event.getPayload())
          .get(10, TimeUnit.SECONDS);

      event.markAsSent();
      log.debug("Sent outbox event: id={}, topic={}", event.getId(), event.getTopic());
    } catch (Exception e) {
      log.error("Failed to publish outbox event: id={}, error={}", event.getId(), e.getMessage());
      event.markAsFailed(e.getMessage());
    }
  }

  @Scheduled(cron = "${outbox.cleanup.cron:0 0 3 * * *}")
  public void cleanupOldEvents() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(7);
    List<Long> ids =
        paymentOutboxReader.findDeleteTargetIds(threshold, PageRequest.of(0, batchSize));
    int deletedCount = paymentOutboxStore.deleteAlreadySentEventByIds(ids);
    if (deletedCount > 0) {
      log.info("Cleaned up {} old sent outbox events", deletedCount);
    }
  }
}
