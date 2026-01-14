package com.modeunsa.shared.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCartItemRequestDto {
  private long productId;
  private int quantity;
}
