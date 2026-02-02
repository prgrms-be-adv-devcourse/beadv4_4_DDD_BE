package com.modeunsa.boundedcontext.payment.domain.exception;

import lombok.Getter;

@Getter
public class PaymentDomainException extends RuntimeException {
  private final PaymentErrorCode errorCode;
  private final String errorMessage;

  public PaymentDomainException(PaymentErrorCode errorCode, String errorMessage, Object... args) {
    super(errorCode.format(args));
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public PaymentDomainException(
      PaymentErrorCode errorCode, String errorMessage, Throwable cause, Object... args) {
    super(errorCode.format(args), cause);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
