package com.modeunsa.global.aop.saga;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaStep {

  String sagaName();

  String step();

  boolean logOnSuccess() default true;

  boolean logOnFailure() default true;
}
