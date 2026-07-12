package com.modeunsa.boundedcontext.member.app.outbox;

import com.modeunsa.boundedcontext.member.domain.entity.MemberOutboxEvent;
import com.modeunsa.boundedcontext.member.out.MemberOutboxStore;
import com.modeunsa.global.eventpublisher.topic.KafkaPublishTarget;
import com.modeunsa.global.eventpublisher.topic.KafkaResolver;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.global.kafka.outbox.OutboxPublisher;
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
public class MemberOutboxPublisher implements OutboxPublisher {

  private final MemberOutboxStore memberOutboxStore;
  private final JsonConverter jsonConverter;
  private final KafkaResolver kafkaResolver;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void saveToOutbox(Object event) {

    KafkaPublishTarget target = kafkaResolver.resolve(event);
    String payload = jsonConverter.serialize(event);
    MemberOutboxEvent outboxEvent =
        MemberOutboxEvent.create(
            target.aggregateType(),
            target.aggregateId(),
            event.getClass().getSimpleName(),
            target.topic(),
            payload,
            target.traceId());

    try {
      memberOutboxStore.store(outboxEvent);
    } catch (DataIntegrityViolationException e) {
      log.warn(
          "Outbox event already exists for aggregateId: {}, eventType: {}",
          outboxEvent.getAggregateId(),
          outboxEvent.getEventType());
    }
  }
}
