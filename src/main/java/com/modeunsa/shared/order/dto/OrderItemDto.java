package com.modeunsa.shared.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemDto {
  private Long productId; // 상품 ID
  private int quantity; // 구매 수량
}
