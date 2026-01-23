package com.modeunsa.boundedcontext.payment.domain.exception;

public class PaymentDomainException extends RuntimeException {
  private final PaymentErrorCode errorCode;

  public PaymentDomainException(PaymentErrorCode errorCode, Object... args) {
    super(errorCode.format(args));
    this.errorCode = errorCode;
  }

  public PaymentDomainException(PaymentErrorCode errorCode, Throwable cause, Object... args) {
    super(errorCode.format(args), cause);
    this.errorCode = errorCode;
  }

  public PaymentErrorCode getErrorCode() {
    return errorCode;
  }
}
