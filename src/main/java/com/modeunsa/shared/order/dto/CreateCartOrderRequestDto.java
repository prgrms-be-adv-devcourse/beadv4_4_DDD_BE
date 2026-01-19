package com.modeunsa.shared.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCartOrderRequestDto {
  @NotNull private String receiverName;
  @NotNull private String receiverPhone;
  @NotNull private String zipcode;
  @NotNull private String addressDetail;
}
