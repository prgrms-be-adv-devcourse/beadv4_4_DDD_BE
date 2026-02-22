package com.modeunsa.global.kafka.inbox;

public interface InboxEventView {

  Long getId();

  String getEventId();

  String getTopic();

  String getPayload();

  String getTraceId();
}
