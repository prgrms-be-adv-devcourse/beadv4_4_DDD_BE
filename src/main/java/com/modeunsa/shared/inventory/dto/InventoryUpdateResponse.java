package com.modeunsa.shared.inventory.dto;

public record InventoryUpdateResponse(Long productId, Long sellerId, Integer quantity) {}
