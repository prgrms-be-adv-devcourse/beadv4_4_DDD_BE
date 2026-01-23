package com.modeunsa.shared.product.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ProductOrderValidateRequest(@NotEmpty List<Long> productIds) {}
