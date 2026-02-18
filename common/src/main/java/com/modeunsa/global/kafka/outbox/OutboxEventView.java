package com.modeunsa.global.kafka.outbox;

public interface OutboxEventView {
  Long getId();

  String getTopic();

  String getPayload();

  String getAggregateId();

  /** 이벤트 타입 (DomainEventEnvelope.eventType 과 맞추기 위해 simple name, 예: PaymentMemberCreatedEvent) */
  String getEventType();

  /** 저장 시점에 부여한 이벤트 ID. 재처리/중복 발행 시 동일 값 유지로 추적·멱등 처리에 사용. */
  String getEventId();

  /** 저장 시점의 트레이스 ID. 분산 추적용. 없으면 null. */
  String getTraceId();

  OutboxStatus getStatus();
}
