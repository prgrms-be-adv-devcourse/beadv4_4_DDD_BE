package com.modeunsa.shared.inventory.dto;

public record InventoryDto(Long productId, Long sellerId, int quantity, int reservedQuantity) {}
