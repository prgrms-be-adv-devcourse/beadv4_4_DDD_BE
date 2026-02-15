package com.modeunsa.global.kafka.outbox;

public interface OutboxEventView {
  Long getId();

  String getTopic();

  String getPayload();

  String getAggregateId();

  /** 이벤트 타입 (DomainEventEnvelope.eventType 과 맞추기 위해 simple name, 예: PaymentMemberCreatedEvent) */
  String getEventType();

  OutboxStatus getStatus();
}
