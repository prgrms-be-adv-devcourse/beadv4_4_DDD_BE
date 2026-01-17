package com.modeunsa.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

@Slf4j
@Aspect
@Component
public class EventListenerLogAspect {

  private static final String LOG_FORMAT_EVENT_STARTED =
      "[{}] [traceId: {}] Event Started - eventType: {}, handler: {}";
  private static final String LOG_FORMAT_EVENT_SUCCEEDED =
      "[{}] [traceId: {}] Event Succeeded - eventType: {}, handler: {}, executionTime: {}ms";
  private static final String LOG_FORMAT_EVENT_FAILED =
      "[{}] [traceId: {}] Event Failed - eventType: {}, handler: {}, "
          + "executionTime: {}ms, error: {}";

  @Pointcut("@annotation(org.springframework.transaction.event.TransactionalEventListener)")
  public void transactionalEventListener() {}

  @Pointcut("@annotation(org.springframework.context.event.EventListener)")
  public void eventListener() {}

  @Around("transactionalEventListener() || eventListener()")
  public Object logEventListener(ProceedingJoinPoint joinPoint) throws Throwable {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    String handlerName = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();
    String eventType = args.length > 0 ? args[0].getClass().getSimpleName() : "Unknown";

    String traceId = MDC.get("TRACE_ID");
    if (!StringUtils.hasText(traceId)) {
      traceId = "N/A";
    }

    log.info(LOG_FORMAT_EVENT_STARTED, handlerName, traceId, eventType, methodName);

    try {
      Object result = joinPoint.proceed();
      stopWatch.stop();

      log.info(
          LOG_FORMAT_EVENT_SUCCEEDED,
          handlerName,
          traceId,
          eventType,
          methodName,
          stopWatch.getTotalTimeMillis());

      return result;
    } catch (Exception e) {
      stopWatch.stop();

      log.error(
          LOG_FORMAT_EVENT_FAILED,
          handlerName,
          traceId,
          eventType,
          methodName,
          stopWatch.getTotalTimeMillis(),
          e.getMessage(),
          e);

      throw e;
    }
  }
}
