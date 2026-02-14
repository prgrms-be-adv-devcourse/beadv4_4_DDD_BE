package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum PaymentOutboxStatus {
  PENDING,
  PROCESSING,
  SENT,
  FAILED
}
