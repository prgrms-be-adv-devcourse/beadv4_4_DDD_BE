package com.modeunsa.global.eventpublisher.topic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DomainEventEnvelope(
    String eventId, String eventType, Instant occurredAt, JsonNode payload) {

  public static DomainEventEnvelope of(Object event, ObjectMapper om) {
    return new DomainEventEnvelope(
        java.util.UUID.randomUUID().toString(),
        event.getClass().getSimpleName(),
        Instant.now(),
        om.valueToTree(event));
  }
}
