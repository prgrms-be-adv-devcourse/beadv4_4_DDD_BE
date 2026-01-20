package com.modeunsa.shared.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartItemsResponseDto(
    long memberId, int totalQuantity, BigDecimal totalAmount, List<CartItemDto> cartItems) {}
