package com.modeunsa.global.kafka.outbox;

public enum OutboxStatus {
  PENDING,
  PROCESSING,
  SENT,
  FAILED
}
