package com.modeunsa.shared.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryUpdateRequest(@NotNull @Positive Integer quantity) {}
