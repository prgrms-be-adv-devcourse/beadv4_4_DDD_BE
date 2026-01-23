package com.modeunsa.shared.order.dto;

import java.math.BigDecimal;

public record CartItemDto(
    Long productId, String name, int quantity, BigDecimal salePrice, Boolean isAvailable) {}
