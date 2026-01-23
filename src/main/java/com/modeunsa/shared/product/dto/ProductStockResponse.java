package com.modeunsa.shared.product.dto;

public record ProductStockResponse(Long productId, boolean success, int remainingStock) {}
