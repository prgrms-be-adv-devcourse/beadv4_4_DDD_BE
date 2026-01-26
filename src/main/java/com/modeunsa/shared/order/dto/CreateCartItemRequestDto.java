package com.modeunsa.shared.order.dto;

import jakarta.validation.constraints.Positive;

public record CreateCartItemRequestDto(long productId, @Positive int quantity) {}
