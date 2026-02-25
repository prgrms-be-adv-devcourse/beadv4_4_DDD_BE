package com.modeunsa.global.kafka.inbox;

import lombok.Getter;

@Getter
public class DuplicateInboxException extends RuntimeException {

  private final String eventId;

  public DuplicateInboxException(String eventId, Throwable cause) {
    super("Duplicate inbox event with eventId: " + eventId, cause);
    this.eventId = eventId;
  }
}
