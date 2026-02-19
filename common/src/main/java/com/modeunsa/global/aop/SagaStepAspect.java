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
    // 기존 MDC 에 남아 있는 값
    String orgTraceId = MDC.get(TRACE_ID_MDC_KEY);
    String orgSaga = MDC.get(MDC_SAGA_NAME);
    String orgStep = MDC.get(MDC_STEP);
    String orgMethodName = MDC.get(MDC_METHOD_NAME);
    String orgDuration = MDC.get(MDC_DURATION);
    String orgStatus = MDC.get(MDC_STATUS);

    String traceId = orgTraceId;
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

    StopWatch stopWatch = new StopWatch();
    boolean success = false;
    Exception failure = null;

    try {
      MDC.put(MDC_STATUS, "START");
      log.info("[SAGA][{}][step={}][traceId={}] 시작 - {}", sagaName, step, traceId, methodName);

      stopWatch.start();
      Object result = joinPoint.proceed();
      success = true;
      return result;
    } catch (Exception e) {
      failure = e;
      throw e;
    } finally {
      if (stopWatch.isRunning()) {
        stopWatch.stop();
      }

      long duration = stopWatch.getTotalTimeMillis();
      MDC.put(MDC_DURATION, duration + "ms");

      if (success) {
        MDC.put(MDC_STATUS, "SUCCESS");
        log.info(
            "[SAGA][{}][step={}][traceId={}] 성공 - {} ({}ms)",
            sagaName,
            step,
            traceId,
            methodName,
            stopWatch.getTotalTimeMillis());
      } else {
        MDC.put(MDC_STATUS, "FAILURE");
        log.error(
            "[SAGA][{}][step={}][traceId={}] 실패 - {} ({}ms) - error={}",
            sagaName,
            step,
            traceId,
            methodName,
            stopWatch.getTotalTimeMillis(),
            failure != null ? failure.getMessage() : "Unknown",
            failure);
      }

      restoreOrRemove(MDC_SAGA_NAME, orgSaga);
      restoreOrRemove(MDC_STEP, orgStep);
      restoreOrRemove(MDC_METHOD_NAME, orgMethodName);
      restoreOrRemove(MDC_DURATION, orgDuration);
      restoreOrRemove(MDC_STATUS, orgStatus);
      restoreOrRemove(TRACE_ID_MDC_KEY, orgTraceId);
    }
  }

  // MDC는 ThreadLocal 기반이라 같은 쓰레드에서 실행되는 코드들이 같은 MDC 맵을 공유한다.
  // 서로 다른 쓰레드(A, B)에서 실행되는 Saga는 서로의 MDC를 공유하지 않지만,
  // 하나의 쓰레드가 여러 Saga/요청을 순차적으로 처리할 수 있기 때문에
  // 현재 SagaStep 실행이 끝날 때 MDC를 원래 상태로 복원해 두어야 이후 작업의 MDC가 오염되지 않는다.
  private void restoreOrRemove(String key, String orgValue) {
    if (StringUtils.hasText(orgValue)) {
      MDC.put(key, orgValue);
    } else {
      MDC.remove(key);
    }
  }
}
