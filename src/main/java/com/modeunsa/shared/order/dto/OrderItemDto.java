package com.modeunsa.shared.order.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemDto {
  private Long id; // 구매상품 ID
  private Long productId; // 상품 ID
  private Long sellerId; // 판매자 ID
  private int quantity; // 구매 수량
  private BigDecimal salePrice; // 판매가
}
