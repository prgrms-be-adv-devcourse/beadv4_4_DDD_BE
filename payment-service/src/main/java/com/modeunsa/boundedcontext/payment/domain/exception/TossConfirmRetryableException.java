package com.modeunsa.boundedcontext.payment.domain.exception;

public class TossConfirmRetryableException extends RuntimeException {

  public TossConfirmRetryableException(String message) {
    super(message);
  }
}
