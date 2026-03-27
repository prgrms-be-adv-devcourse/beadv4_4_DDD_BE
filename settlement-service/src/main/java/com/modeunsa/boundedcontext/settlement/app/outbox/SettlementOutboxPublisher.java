package com.modeunsa.boundedcontext.settlement.app.outbox;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.boundedcontext.settlement.out.SettlementOutboxStore;
import com.modeunsa.global.eventpublisher.topic.KafkaPublishTarget;
import com.modeunsa.global.eventpublisher.topic.KafkaResolver;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.global.kafka.outbox.OutboxPublisher;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "outbox")
public class SettlementOutboxPublisher implements OutboxPublisher {

  private final SettlementOutboxStore settlementOutboxStore;
  private final JsonConverter jsonConverter;
  private final KafkaResolver kafkaResolver;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void saveToOutbox(Object event) {
    SettlementCompletedPayoutEvent payoutEvent = (SettlementCompletedPayoutEvent) event;

    KafkaPublishTarget target = kafkaResolver.resolve(payoutEvent);
    String payload = jsonConverter.serialize(payoutEvent);
    SettlementOutboxEvent outboxEvent =
        SettlementOutboxEvent.create(
            target.aggregateType(),
            target.aggregateId(),
            payoutEvent.getClass().getSimpleName(),
            target.topic(),
            payload,
            payoutEvent.eventId(),
            payoutEvent.traceId());

    try {
      settlementOutboxStore.store(outboxEvent);
      settlementOutboxStore.flush();
    } catch (DataIntegrityViolationException e) {
      log.warn(
          "Outbox event already exists for eventId: {}, aggregateId: {}",
          outboxEvent.getEventId(),
          outboxEvent.getAggregateId());
    }
  }
}
