package com.modeunsa.shared.inventory.dto;

import java.util.List;

public record InventoryListRequest(List<Long> productIds) {}
