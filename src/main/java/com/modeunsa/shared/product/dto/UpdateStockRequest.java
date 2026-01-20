package com.modeunsa.shared.product.dto;

import java.util.List;

public record UpdateStockRequest(Long orderId, List<ProductOrderItemDto> items) {
  public record ProductOrderItemDto(Long productId, int quantity) {}
}
