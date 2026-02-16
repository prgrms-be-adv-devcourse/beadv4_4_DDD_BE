package com.modeunsa.global.kafka.outbox;

import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class OutboxPollerRunner {

  private final OutboxPollerRunner self;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  // self 참조를 주입받아 트랜잭션이 적용된 메서드를 내부에서 호출할 때 프록시를 통해 호출되도록 함
  public OutboxPollerRunner(
      @Lazy OutboxPollerRunner self, KafkaTemplate<String, Object> kafkaTemplate) {
    this.self = self;
    this.kafkaTemplate = kafkaTemplate;
  }

  public void runPolling(
      OutboxReader reader, OutboxStore store, int batchSize, int maxRetry, int timeoutSeconds) {
    List<? extends OutboxEventView> pending = self.findPendingEvents(reader, store, batchSize);

    for (OutboxEventView event : pending) {
      try {
        DomainEventEnvelope envelope =
            new DomainEventEnvelope(
                getEventId(event.getEventId()),
                event.getEventType(),
                Instant.now(),
                event.getPayload(),
                event.getTraceId());
        kafkaTemplate
            .send(event.getTopic(), event.getAggregateId(), envelope)
            .get(timeoutSeconds, TimeUnit.SECONDS);

        store.markSent(event.getId());
      } catch (Exception e) {
        log.error(
            "Failed to publish outbox event: id={}, trace_id={} topic={} error={}",
            event.getId(),
            event.getTraceId(),
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

  // Lock을 획득한 상태로 마킹하여 다른 Poller가 동시에 처리하지 못하도록 함
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<? extends OutboxEventView> findPendingEvents(
      OutboxReader reader, OutboxStore store, int batchSize) {
    List<? extends OutboxEventView> pending =
        reader.findPendingEventsWithLock(PageRequest.of(0, batchSize));
    for (OutboxEventView event : pending) {
      store.markProcessing(event.getId());
    }
    return pending;
  }

  private String getEventId(String eventId) {
    return StringUtils.hasText(eventId) ? eventId : UUID.randomUUID().toString();
  }
}
