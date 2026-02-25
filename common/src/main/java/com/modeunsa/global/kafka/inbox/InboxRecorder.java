package com.modeunsa.global.kafka.inbox;

public interface InboxRecorder {
  void recordOrThrowDuplicate(String eventId, String topic, String payload, String traceId);
}
