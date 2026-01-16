package com.modeunsa.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

  @Pointcut("execution(* com.modeunsa.boundedcontext..*Controller.*(..))")
  public void controllerMethods() {}

  @Around("controllerMethods()")
  public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    HttpServletRequest request = getHttpServletRequest();
    String httpMethod = request != null ? request.getMethod() : "N/A";
    String requestUri = request != null ? request.getRequestURI() : "N/A";
    String queryString = request != null ? request.getQueryString() : null;
    String fullUri = queryString != null ? requestUri + "?" + queryString : requestUri;

    // 요청 로깅
    log.info(
        "[{}] {} {} - method: {}.{}, parameter: {}",
        className,
        httpMethod,
        fullUri,
        className,
        methodName,
        args);

    try {
      Object result = joinPoint.proceed();
      stopWatch.stop();

      // 응답 로깅
      log.info(
          "[{}] {} {} - method: {}.{}, executionTime: {}ms, status: SUCCESS",
          className,
          httpMethod,
          fullUri,
          className,
          methodName,
          stopWatch.getTotalTimeMillis());

      return result;
    } catch (Exception e) {
      stopWatch.stop();
      log.error(
          "[{}] {} {} - method: {}.{}, executionTime: {}ms, status: FAILURE, error: {}",
          className,
          httpMethod,
          fullUri,
          className,
          methodName,
          stopWatch.getTotalTimeMillis(),
          e.getMessage(),
          e);
      throw e;
    }
  }

  private HttpServletRequest getHttpServletRequest() {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      return attributes != null ? attributes.getRequest() : null;
    } catch (Exception e) {
      return null;
    }
  }
}
