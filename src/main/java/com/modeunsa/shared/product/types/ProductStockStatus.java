package com.modeunsa.shared.product.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStockStatus {
  STOCK_SUCCESS("재고 차감 성공"),
  STOCK_FAILED("재고 차감 실패");

  private final String description;
}
