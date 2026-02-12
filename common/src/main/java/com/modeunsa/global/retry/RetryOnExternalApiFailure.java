package com.modeunsa.global.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * 외부 API 호출 시 네트워크 문제 등 일시적 오류 발생 시 재시도 어노테이션 ResourceAccessException : 네트워크 문제, 타임아웃 등
 * HttpServerErrorException.BadGateway : 502 Bad Gateway HttpServerErrorException.GatewayTimeout :
 * 504 Gateway Timeout HttpServerErrorException.ServiceUnavailable : 503 Service Unavailable
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    retryFor = {
      ResourceAccessException.class,
      HttpServerErrorException.BadGateway.class,
      HttpServerErrorException.GatewayTimeout.class,
      HttpServerErrorException.ServiceUnavailable.class
    },
    noRetryFor = {
      HttpClientErrorException.class,
      HttpServerErrorException.InternalServerError.class
    },
    maxAttempts = 3,
    backoff = @Backoff(delay = 500, multiplier = 2))
public @interface RetryOnExternalApiFailure {}
