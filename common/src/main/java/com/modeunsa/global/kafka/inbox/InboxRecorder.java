package com.modeunsa.global.kafka.inbox;

public interface InboxRecorder {
  boolean tryRecord(String eventId, String topic, String payload, String traceId);
}
