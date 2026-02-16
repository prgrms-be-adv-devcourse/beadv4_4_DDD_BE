package com.modeunsa.global.kafka.outbox;

import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollerRunner {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void runPolling(
      OutboxReader reader, OutboxStore store, int batchSize, int maxRetry, int timeoutSeconds) {
    List<? extends OutboxEventView> pending =
        reader.findPendingEvents(PageRequest.of(0, batchSize));

    for (OutboxEventView event : pending) {
      try {
        store.markProcessing(event.getId());
        String traceId = event.getTraceId();
        DomainEventEnvelope envelope =
            new DomainEventEnvelope(
                getEventId(event.getEventId()),
                event.getEventType(),
                Instant.now(),
                event.getPayload(),
                traceId);
        SendResult<String, Object> result =
            kafkaTemplate
                .send(event.getTopic(), event.getAggregateId(), envelope)
                .get(timeoutSeconds, TimeUnit.SECONDS);

        store.markSent(event.getId());
      } catch (Exception e) {
        log.error(
            "Failed to publish outbox event: id={}, topic={} error={}",
            event.getId(),
            event.getTopic(),
            e.getMessage(),
            e);
        store.markFailed(event.getId(), e.getMessage(), maxRetry);
      }
    }
  }

  public void runCleanup(
      OutboxReader reader, OutboxStore store, LocalDateTime before, int batchSize) {
    List<Long> ids = reader.findDeleteTargetIds(before, PageRequest.of(0, batchSize));
    long deleted = store.deleteAlreadySentEventByIds(ids);
    if (deleted > 0) {
      log.info("Outbox cleanup: deleted {} events", deleted);
    }
  }

  private String getEventId(String eventId) {
    return StringUtils.hasText(eventId) ? eventId : UUID.randomUUID().toString();
  }
}
