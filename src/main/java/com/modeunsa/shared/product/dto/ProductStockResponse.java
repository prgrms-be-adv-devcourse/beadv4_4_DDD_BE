package com.modeunsa.shared.product.dto;

public record ProductStockResponse(Long productId, boolean isSucceed, int remainingStock) {}
