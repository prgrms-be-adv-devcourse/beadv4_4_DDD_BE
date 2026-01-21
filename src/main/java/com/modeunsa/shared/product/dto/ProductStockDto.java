package com.modeunsa.shared.product.dto;

public record ProductStockDto(Long productId, boolean success, int remainingStock) {}
