package com.modeunsa.global.kafka.outbox;

public interface OutboxPublisher {
  void saveToOutbox(Object event);
}
