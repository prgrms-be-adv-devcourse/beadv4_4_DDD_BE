package com.modeunsa.shared.product.dto.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductSearchRequest(
    Long id,
    String name,
    String sellerBusinessName,
    String description,
    String category,
    String saleStatus,
    BigDecimal salePrice,
    String primaryImageUrl,
    LocalDateTime createdAt) {}
