package com.modeunsa.global.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

  private static final String EXECUTION_STOP_WATCH = "executionStopWatch";
  private static final String EXCEPTION_ATTRIBUTE = "handledException";

  private static final String LOG_FORMAT_REQUEST_STARTED =
      "[{}] [traceId: {}] {} {} - Request Started";
  private static final String LOG_FORMAT_REQUEST_SUCCEEDED =
      "[{}] [traceId: {}] {} {} - Request succeeded, executionTime: {}ms, status: {}";
  private static final String LOG_FORMAT_REQUEST_FAILED =
      "[{}] [traceId: {}] {} {} - Request failed, executionTime: {}ms, status: {}, error: {}";

  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";
  private static final String UNKNOWN_TRACE_ID = "Unknown";

  private record RequestInfo(
      String controllerName, String traceId, String method, String fullUri) {}

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    request.setAttribute(EXECUTION_STOP_WATCH, stopWatch);

    RequestInfo requestInfo = extractRequestInfo(request, handler);

    log.info(
        LOG_FORMAT_REQUEST_STARTED,
        requestInfo.controllerName(),
        requestInfo.traceId(),
        requestInfo.method(),
        requestInfo.fullUri());

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

    RequestInfo requestInfo = extractRequestInfo(request, handler);
    long executionTime = (stopWatch != null) ? stopWatch.getTotalTimeMillis() : 0;
    Exception exception = getException(ex, request);
    int statusCode = response.getStatus();

    if (exception != null) {
      logErrorRequest(requestInfo, executionTime, statusCode, exception);
    } else {
      log.info(
          LOG_FORMAT_REQUEST_SUCCEEDED,
          requestInfo.controllerName(),
          requestInfo.traceId(),
          requestInfo.method(),
          requestInfo.fullUri(),
          executionTime,
          statusCode);
    }
  }

  private void logErrorRequest(
      RequestInfo requestInfo, long executionTime, int statusCode, Exception exception) {

    boolean loggingStackTrace = statusCode >= 500;
    if (loggingStackTrace) {
      log.error(
          LOG_FORMAT_REQUEST_FAILED,
          requestInfo.controllerName(),
          requestInfo.traceId(),
          requestInfo.method(),
          requestInfo.fullUri(),
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
    return StringUtils.hasText(traceId) ? traceId : UNKNOWN_TRACE_ID;
  }

  private String extractHandlerName(Object handler) {
    if (handler == null) {
      return UNKNOWN_TRACE_ID;
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
}
