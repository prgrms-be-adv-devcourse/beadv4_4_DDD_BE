package com.modeunsa.shared.event;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public abstract class BaseEvent {

  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";
  private static final String UNKNOWN_TRACE_ID = "Unknown";

  private final String traceId;

  protected BaseEvent() {
    String traceIdFromMdc = MDC.get(TRACE_ID_MDC_KEY);
    this.traceId = StringUtils.hasText(traceIdFromMdc) ? traceIdFromMdc : UNKNOWN_TRACE_ID;
  }

  protected BaseEvent(String traceId) {
    this.traceId = traceId;
  }

  public String getTraceId() {
    return traceId;
  }
}
