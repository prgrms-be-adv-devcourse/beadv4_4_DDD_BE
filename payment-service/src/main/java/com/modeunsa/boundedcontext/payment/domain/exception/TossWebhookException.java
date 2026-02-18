package com.modeunsa.boundedcontext.payment.domain.exception;

import lombok.Getter;

@Getter
public class TossWebhookException extends RuntimeException {

  private final TossWebhookErrorCode errorCode;

  public TossWebhookException(TossWebhookErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
