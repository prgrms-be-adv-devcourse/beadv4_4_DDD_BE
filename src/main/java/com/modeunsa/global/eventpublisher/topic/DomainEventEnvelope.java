package com.modeunsa.global.eventpublisher.topic;

import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record DomainEventEnvelope(
    String eventId, String eventType, Instant occurredAt, JsonNode payload) {

  public static DomainEventEnvelope of(Object event, ObjectMapper om) {
    return new DomainEventEnvelope(
        UUID.randomUUID().toString(),
        event.getClass().getSimpleName(),
        Instant.now(),
        om.valueToTree(event));
  }
}
