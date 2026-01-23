package com.modeunsa.shared.product.dto;

import java.math.BigDecimal;

public record ProductOrderResponse(
    Long productId,
    Long sellerId,
    String name,
    BigDecimal salePrice, // 판매가
    BigDecimal price, // 정가
    int stock,
    boolean isAvailable) {}
