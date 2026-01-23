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

  @NotNull private String recipientName;
  @NotNull private String recipientPhone;
  @NotNull private String zipCode;
  @NotNull private String address;
  @NotNull private String addressDetail;
}
