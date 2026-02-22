package com.modeunsa.global.eventpublisher;

import com.modeunsa.global.kafka.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;

/** Kafka 발행은 Outbox 폴러가 나중에 수행한다. */
@RequiredArgsConstructor
public class OutboxDomainEventPublisher implements EventPublisher {

  private final OutboxPublisher outboxPublisher;

  @Override
  public void publish(Object event) {
    outboxPublisher.saveToOutbox(event);
  }
}
