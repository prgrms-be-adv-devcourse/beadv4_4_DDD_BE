package com.modeunsa.global.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.resilience.annotation.Retryable;

/**
 * 일시적 DB 접근 오류 발생 시 재시도 어노테이션 TransientDataAccessException : DB 커넥션 문제, 타임아웃 등 일시적 오류
 * DataAccessResourceFailureException : DB 리소스 접근 실패 등 일시적 오류 Retry 는 멱등성이 보장된 메서드에만 적용해야 함
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    includes = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
    maxRetries = 2,
    delay = 500,
    multiplier = 2)
public @interface RetryOnDbFailure {}
