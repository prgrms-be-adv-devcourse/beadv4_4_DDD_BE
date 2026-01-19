package com.modeunsa.global.event;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * 이벤트 생성 시 traceId를 추출하는 유틸리티 클래스.
 *
 * <p>MDC에서 traceId를 가져와 이벤트에 포함시킬 수 있도록 합니다. traceId가 없으면 "Unknown"을 반환합니다.
 */
public final class EventUtils {

  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";
  private static final String UNKNOWN_TRACE_ID = "Unknown";

  private EventUtils() {}

  /**
   * MDC에서 traceId를 추출합니다.
   *
   * @return MDC에 저장된 traceId, 없으면 "Unknown"
   */
  public static String extractTraceId() {
    String traceId = MDC.get(TRACE_ID_MDC_KEY);
    return StringUtils.hasText(traceId) ? traceId : UNKNOWN_TRACE_ID;
  }
}
