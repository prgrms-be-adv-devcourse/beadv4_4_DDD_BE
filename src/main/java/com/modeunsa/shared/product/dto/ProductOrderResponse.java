package com.modeunsa.shared.product.dto;

import java.math.BigDecimal;

public record ProductOrderResponse(
    Long productId,
    String name,
    BigDecimal salePrice, // 판매가
    int stock,
    boolean isAvailable) {}
