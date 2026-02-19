package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum PayoutEventType {
  FEE("수수료"),
  AMOUNT("판매대금");

  private final String description;

  PayoutEventType(String description) {
    this.description = description;
  }
}
