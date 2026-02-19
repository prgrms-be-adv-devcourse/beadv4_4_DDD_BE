package com.modeunsa.global.aop;

import com.modeunsa.global.aop.saga.OrderSagaStep;
import com.modeunsa.global.aop.saga.SagaStep;
import com.modeunsa.global.aop.saga.SagaType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

@Aspect
@Component
public class SagaStepAspect {

  private static final Logger log = LoggerFactory.getLogger(SagaStepAspect.class);
  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";
  private static final String UNKNOWN_TRACE_ID = "UNKNOWN";

  private static final String MDC_SAGA_NAME = "saga";
  private static final String MDC_STEP = "step";
  private static final String MDC_METHOD_NAME = "methodName";
  private static final String MDC_DURATION = "duration";
  private static final String MDC_STATUS = "status";

  @Around("@annotation(sagaStep)")
  public Object logSagaStep(ProceedingJoinPoint joinPoint, SagaStep sagaStep) throws Throwable {
    String traceId = MDC.get(TRACE_ID_MDC_KEY);
    if (!StringUtils.hasText(traceId)) {
      traceId = UNKNOWN_TRACE_ID;
    }

    SagaType sagaType = sagaStep.sagaName();
    String sagaName = sagaType.name();
    OrderSagaStep step = sagaStep.step();
    String methodName = joinPoint.getSignature().getName();

    MDC.put(MDC_SAGA_NAME, sagaName);
    MDC.put(MDC_STEP, step.name());
    MDC.put(MDC_METHOD_NAME, methodName);
    MDC.put(TRACE_ID_MDC_KEY, traceId);

    MDC.put(MDC_STATUS, "START");
    log.info("[SAGA][{}][step={}][traceId={}] 시작 - {}", sagaName, step, traceId, methodName);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      Object result = joinPoint.proceed();
      stopWatch.stop();

      if (sagaStep.logOnSuccess()) {
        MDC.put(MDC_STATUS, "SUCCESS");
        MDC.put(MDC_DURATION, String.valueOf(stopWatch.getTotalTimeMillis()));
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
        MDC.put(MDC_STATUS, "FAILURE");
        MDC.put(MDC_DURATION, String.valueOf(stopWatch.getTotalTimeMillis()));
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
