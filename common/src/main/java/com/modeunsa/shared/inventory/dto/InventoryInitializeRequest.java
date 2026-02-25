package com.modeunsa.shared.inventory.dto;

import jakarta.validation.constraints.Positive;

public record InventoryInitializeRequest(@Positive int quantity) {}
