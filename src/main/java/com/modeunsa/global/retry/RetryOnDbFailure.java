package com.modeunsa.global.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    retryFor = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 500, multiplier = 2))
public @interface RetryOnDbFailure {}
