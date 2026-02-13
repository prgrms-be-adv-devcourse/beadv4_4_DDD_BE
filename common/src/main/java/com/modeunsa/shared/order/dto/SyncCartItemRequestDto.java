package com.modeunsa.shared.order.dto;

import jakarta.validation.constraints.Positive;

public record SyncCartItemRequestDto(long productId, @Positive int quantity) {}
