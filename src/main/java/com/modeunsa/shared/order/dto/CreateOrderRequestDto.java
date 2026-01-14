package com.modeunsa.shared.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDto {
  @NotNull private Long productId;

  @Positive private int quantity;

  @NotNull private String receiverName;
  @NotNull private String receiverPhone;
  @NotNull private String zipcode;
  @NotNull private String addressDetail;
}
