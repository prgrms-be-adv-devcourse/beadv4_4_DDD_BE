package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum ProviderType {
  MODEUNSA_PAY("뭐든사 페이"),
  TOSS_PAYMENTS("토스 페이먼츠");

  private final String description;

  ProviderType(String description) {
    this.description = description;
  }
}
