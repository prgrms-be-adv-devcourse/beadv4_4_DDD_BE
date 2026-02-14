package com.modeunsa.global.kafka.outbox;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollerRunner {

  private final KafkaTemplate<String, String> outboxKafkaTemplate;

  public void runPolling(OutboxReader reader, OutboxStore store, int batchSize, int maxRetry) {
    List<? extends OutboxEventView> pending =
        reader.findPendingEvents(PageRequest.of(0, batchSize));

    for (OutboxEventView event : pending) {
      try {
        store.markProcessing(event.getId());
        outboxKafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload());
        store.markSent(event.getId());
      } catch (Exception e) {
        log.error(
            "Failed to publish outbox event: id={}, topic={} error={}",
            event.getId(),
            event.getTopic(),
            e.getMessage());
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
}
