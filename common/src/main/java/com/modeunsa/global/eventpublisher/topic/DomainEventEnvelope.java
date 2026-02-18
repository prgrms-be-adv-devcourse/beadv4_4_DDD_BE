package com.modeunsa.global.eventpublisher.topic;

import com.modeunsa.global.event.TraceableEvent;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

public record DomainEventEnvelope(
    String eventId,
    String eventType,
    Instant occurredAt,
    String topic,
    String payload,
    String traceId) {

  public static DomainEventEnvelope of(Object event, String topic, String payload) {
    String traceId = extractTraceId(event);
    return new DomainEventEnvelope(
        UUID.randomUUID().toString(),
        event.getClass().getSimpleName(),
        Instant.now(),
        topic,
        payload,
        traceId);
  }

  private static String extractTraceId(Object event) {
    if (event instanceof TraceableEvent te) {
      return te.traceId();
    }
    return MDC.get("TRACE_ID");
  }
}
