package com.modeunsa.global.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    includes = {PessimisticLockingFailureException.class, CannotAcquireLockException.class},
    maxRetries = 2,
    delay = 100,
    multiplier = 2)
public @interface RetryOnDbLockFailure {}
