package com.modeunsa.shared.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryUpdateRequest(@NotNull @PositiveOrZero Integer quantity) {}
