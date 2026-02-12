package com.modeunsa.shared.product.dto.search;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductSearchResponse(
    String id,
    String name,
    String description,
    String category,
    String saleStatus,
    BigDecimal price,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
