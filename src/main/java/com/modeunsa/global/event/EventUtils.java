package com.modeunsa.global.event;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public final class EventUtils {

  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";
  private static final String UNKNOWN_TRACE_ID = "Unknown";

  private EventUtils() {}

  public static String extractTraceId() {
    String traceId = MDC.get(TRACE_ID_MDC_KEY);
    return StringUtils.hasText(traceId) ? traceId : UNKNOWN_TRACE_ID;
  }
}
