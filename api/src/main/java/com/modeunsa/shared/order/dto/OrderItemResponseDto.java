package com.modeunsa.shared.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponseDto {
  private Long productId;
  private String productName;
  private int quantity;
  private BigDecimal salePrice;
}
