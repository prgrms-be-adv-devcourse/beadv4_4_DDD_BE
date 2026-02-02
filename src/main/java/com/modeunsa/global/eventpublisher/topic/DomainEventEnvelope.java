package com.modeunsa.global.eventpublisher.topic;

import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.global.json.JsonConverter;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

public record DomainEventEnvelope(
    String eventId, String eventType, Instant occurredAt, String payload, String traceId) {

  public static DomainEventEnvelope of(Object event, JsonConverter jsonConverter) {
    String traceId = extractTraceId(event);
    return new DomainEventEnvelope(
        UUID.randomUUID().toString(),
        event.getClass().getSimpleName(),
        Instant.now(),
        jsonConverter.serialize(event),
        traceId);
  }

  private static String extractTraceId(Object event) {
    if (event instanceof TraceableEvent te) {
      return te.traceId();
    }
    return MDC.get("TRACE_ID");
  }
}
