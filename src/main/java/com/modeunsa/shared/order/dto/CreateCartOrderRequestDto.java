package com.modeunsa.shared.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCartOrderRequestDto {
  @NotNull private String recipientName;
  @NotNull private String recipientPhone;
  @NotNull private String zipCode;
  @NotNull private String address;
  @NotNull private String addressDetail;
}
