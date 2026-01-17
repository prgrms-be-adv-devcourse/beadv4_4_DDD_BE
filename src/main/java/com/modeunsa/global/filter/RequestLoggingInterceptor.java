package com.modeunsa.global.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

  private static final String EXECUTION_START_TIME = "executionStartTime";
  private static final String EXECUTION_STOP_WATCH = "executionStopWatch";
  private static final String EXCEPTION_ATTRIBUTE = "handledException";

  private static final String LOG_FORMAT_REQUEST_COMPLETED =
      "[{}] [traceId: {}] {} {} - startTime: {}, executionTime: {}ms, status: {}";
  private static final String LOG_FORMAT_REQUEST_FAILED =
      "[{}] [traceId: {}] {} {} - startTime: {}, executionTime: {}ms, status: {}, error: {}";

  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";
  private static final String UNKNOWN = "Unknown";

  private record RequestInfo(
      String controllerName, String traceId, String method, String fullUri) {}

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    long startTime = System.currentTimeMillis();
    request.setAttribute(EXECUTION_START_TIME, startTime);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    request.setAttribute(EXECUTION_STOP_WATCH, stopWatch);

    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      @Nullable Exception ex)
      throws Exception {

    StopWatch stopWatch = (StopWatch) request.getAttribute(EXECUTION_STOP_WATCH);
    if (stopWatch != null) {
      stopWatch.stop();
    }

    Long startTime = (Long) request.getAttribute(EXECUTION_START_TIME);
    long executionTime =
        (stopWatch != null && stopWatch.isRunning()) ? stopWatch.getTotalTimeMillis() : 0;

    RequestInfo requestInfo = extractRequestInfo(request, handler);
    Exception exception = getException(ex, request);
    int statusCode = response.getStatus();

    if (exception != null) {
      logErrorRequest(requestInfo, startTime, executionTime, statusCode, exception);
    } else {
      log.info(
          LOG_FORMAT_REQUEST_COMPLETED,
          requestInfo.controllerName(),
          requestInfo.traceId(),
          requestInfo.method(),
          requestInfo.fullUri(),
          formatTimestamp(startTime),
          executionTime,
          statusCode);
    }
  }

  private void logErrorRequest(
      RequestInfo requestInfo,
      Long startTime,
      long executionTime,
      int statusCode,
      Exception exception) {

    boolean loggingStackTrace = statusCode >= 500;
    if (loggingStackTrace) {
      log.error(
          LOG_FORMAT_REQUEST_FAILED,
          requestInfo.controllerName(),
          requestInfo.traceId(),
          requestInfo.method(),
          requestInfo.fullUri(),
          formatTimestamp(startTime),
          executionTime,
          statusCode,
          exception.getMessage(),
          exception);
    } else {
      log.warn(
          LOG_FORMAT_REQUEST_FAILED,
          requestInfo.controllerName(),
          requestInfo.traceId(),
          requestInfo.method(),
          requestInfo.fullUri(),
          formatTimestamp(startTime),
          executionTime,
          statusCode,
          exception.getMessage());
    }
  }

  private RequestInfo extractRequestInfo(HttpServletRequest request, Object handler) {
    String traceId = getTraceId();
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String fullUri = (queryString != null) ? uri + "?" + queryString : uri;
    String controllerName = extractHandlerName(handler);

    return new RequestInfo(controllerName, traceId, method, fullUri);
  }

  private String getTraceId() {
    String traceId = MDC.get(TRACE_ID_MDC_KEY);
    return StringUtils.hasText(traceId) ? traceId : UNKNOWN;
  }

  private String extractHandlerName(Object handler) {
    if (handler == null) {
      return UNKNOWN;
    }
    if (handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      return handlerMethod.getBeanType().getSimpleName();
    }
    return handler.getClass().getSimpleName();
  }

  private Exception getException(@Nullable Exception ex, HttpServletRequest request) {
    if (ex != null) {
      return ex;
    }
    Object exceptionAttr = request.getAttribute(EXCEPTION_ATTRIBUTE);
    return exceptionAttr instanceof Exception ? (Exception) exceptionAttr : null;
  }

  private String formatTimestamp(Long timestamp) {
    if (timestamp == null) {
      return UNKNOWN;
    }
    Instant instant = Instant.ofEpochMilli(timestamp);
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
  }
}
