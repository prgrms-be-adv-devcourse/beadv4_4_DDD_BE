package com.modeunsa.global.eventpublisher.topic;

import com.modeunsa.global.event.TraceableEvent;
import java.time.Instant;
import org.slf4j.MDC;

public record DomainEventEnvelope(
    String eventId,
    String eventType,
    Instant occurredAt,
    String topic,
    String payload,
    String traceId) {

  public static DomainEventEnvelope of(
      Object event, String aggregateType, String aggregateId, String topic, String payload) {
    String traceId = extractTraceId(event);
    String eventType = event.getClass().getSimpleName();
    return new DomainEventEnvelope(
        buildEventId(aggregateType, aggregateId, eventType),
        eventType,
        Instant.now(),
        topic,
        payload,
        traceId);
  }

  private static String buildEventId(String aggregateType, String aggregateId, String eventType) {
    return String.format("%s-%s-%s", aggregateType, aggregateId, eventType);
  }

  private static String extractTraceId(Object event) {
    if (event instanceof TraceableEvent te) {
      return te.traceId();
    }
    return MDC.get("TRACE_ID");
  }
}
