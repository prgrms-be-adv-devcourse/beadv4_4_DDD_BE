package com.modeunsa.boundedcontext.product.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductCurrency {
  KRW("원"),
  USD("달러"),
  JPY("엔");

  private final String value;
}
