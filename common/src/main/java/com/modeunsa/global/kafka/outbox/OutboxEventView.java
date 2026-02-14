package com.modeunsa.global.kafka.outbox;

public interface OutboxEventView {
  Long getId();

  String getTopic();

  String getPayload();

  String getAggregateId();

  OutboxStatus getStatus();
}
