package com.modeunsa.global.eventpublisher.topic;

import com.modeunsa.global.json.JsonConverter;
import java.time.Instant;
import java.util.UUID;

public record DomainEventEnvelope(
    String eventId, String eventType, Instant occurredAt, String payload) {

  public static DomainEventEnvelope of(Object event, JsonConverter jsonConverter) {
    return new DomainEventEnvelope(
        UUID.randomUUID().toString(),
        event.getClass().getSimpleName(),
        Instant.now(),
        jsonConverter.serialize(event));
  }
}
