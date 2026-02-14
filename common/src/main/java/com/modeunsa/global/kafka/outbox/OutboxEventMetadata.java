package com.modeunsa.global.kafka.outbox;

/** Outbox 이벤트 저장 시 필요한 메타데이터. 각 모듈의 OutboxPublisher가 이벤트에서 추출하여 사용한다. */
public record OutboxEventMetadata(String aggregateType, String aggregateId, String topic) {}
