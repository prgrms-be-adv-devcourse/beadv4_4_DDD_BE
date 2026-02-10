package com.modeunsa.shared.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderDeliveryRequestDto(
    @NotNull @NotEmpty @NotBlank String recipientName,
    @NotNull @NotEmpty @NotBlank String recipientPhone,
    @NotNull @NotEmpty @NotBlank String zipCode,
    @NotNull @NotEmpty @NotBlank String address,
    @NotNull @NotEmpty @NotBlank String addressDetail) {}
