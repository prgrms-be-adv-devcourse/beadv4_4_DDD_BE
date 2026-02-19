package com.modeunsa.boundedcontext.payment.domain.exception;

import lombok.Getter;

@Getter
public class TossConfirmFailedException extends RuntimeException {

  private final PaymentErrorCode errorCode;
  private final String tossCode;
  private final String tossMessage;

  public TossConfirmFailedException(PaymentErrorCode errorCode, String message) {
    this(errorCode, null, null, message);
  }

  public TossConfirmFailedException(
      PaymentErrorCode errorCode, String tossCode, String tossMessage, String message) {
    super(message);
    this.errorCode = errorCode;
    this.tossCode = tossCode;
    this.tossMessage = tossMessage;
  }
}
