package com.modeunsa.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(1)
public class TraceIdFilter extends OncePerRequestFilter {

  private static final String TRACE_ID_HEADER = "X-Trace-Id";
  private static final String TRACE_ID_KEY = "TRACE_ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      // 1. HTTP 헤더에서 traceId 추출 (없으면 생성)
      String traceId = extractOrGenerateTraceId(request);

      // 2. MDC에 traceId 저장
      MDC.put(TRACE_ID_KEY, traceId);

      // 3. 응답 헤더에도 traceId 포함
      response.setHeader(TRACE_ID_HEADER, traceId);

      filterChain.doFilter(request, response);
    } finally {
      // 4. 요청 처리 완료 후 MDC 정리
      MDC.clear();
    }
  }

  private String extractOrGenerateTraceId(HttpServletRequest request) {
    String traceId = request.getHeader(TRACE_ID_HEADER);

    if (StringUtils.hasText(traceId)) {
      return traceId;
    }

    return UUID.randomUUID().toString();
  }
}
