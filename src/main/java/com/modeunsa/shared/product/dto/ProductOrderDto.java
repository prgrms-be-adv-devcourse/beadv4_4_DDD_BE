package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import java.math.BigDecimal;

public record ProductOrderDto(
    Long productId,
    String name,
    BigDecimal salePrice, // 판매가
    int stock,
    SaleStatus saleStatus,
    boolean isAvailable) {

  public ProductOrderDto setIsAvailable(boolean isAvailable) {
    return new ProductOrderDto(
        this.productId, this.name, this.salePrice, this.stock, this.saleStatus, isAvailable);
  }
}
