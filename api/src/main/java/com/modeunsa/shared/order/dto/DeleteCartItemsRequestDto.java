package com.modeunsa.shared.order.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DeleteCartItemsRequestDto(@NotNull List<Long> cartItemIds) {}
