package com.modeunsa.shared.inventory.dto;

import java.util.List;

public record InventoryListResponse(List<InventoryDto> productIds) {}
