package com.modeunsa.boundedcontext.product.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SaleStatus {
  SALE("판매중"),
  NOT_SALE("판매중지");

  private final String description;
}
