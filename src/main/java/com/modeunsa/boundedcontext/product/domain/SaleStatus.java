package com.modeunsa.boundedcontext.product.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SaleStatus {
  SALE("판매중"),
  NOT_SALE("판매중지"),
  SOLD_OUT("품절");

  @Column(name="\"value\"")
  private final String value;
}
