package com.modeunsa.global.aop;

import com.modeunsa.global.aop.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

@Aspect
@Component
@Slf4j
public class SagaStepAspect {

  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";
  private static final String UNKNOWN_TRACE_ID = "UNKNOWN";

  @Around("@annotation(sagaStep)")
  public Object logSagaStep(ProceedingJoinPoint joinPoint, SagaStep sagaStep) throws Throwable {
    String traceId = MDC.get(TRACE_ID_MDC_KEY);
    if (!StringUtils.hasText(traceId)) {
      traceId = UNKNOWN_TRACE_ID;
    }

    String sagaName = sagaStep.sagaName();
    String step = sagaStep.step();
    String methodName = joinPoint.getSignature().getName();

    log.info("[SAGA][{}][step={}][traceId={}] 시작 - {}", sagaName, step, traceId, methodName);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      Object result = joinPoint.proceed();
      stopWatch.stop();

      if (sagaStep.logOnSuccess()) {
        log.info(
            "[SAGA][{}][step={}][traceId={}] 성공 - {} ({}ms)",
            sagaName,
            step,
            traceId,
            methodName,
            stopWatch.getTotalTimeMillis());
      }

      return result;
    } catch (Exception e) {
      stopWatch.stop();

      if (sagaStep.logOnFailure()) {
        log.error(
            "[SAGA][{}][step={}][traceId={}] 실패 - {} ({}ms), error: {}",
            sagaName,
            step,
            traceId,
            methodName,
            stopWatch.getTotalTimeMillis(),
            e.getMessage(),
            e);
      }
      throw e;
    }
  }
}
