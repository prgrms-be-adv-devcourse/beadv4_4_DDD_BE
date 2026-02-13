package com.modeunsa.shared.product.dto;

import java.math.BigDecimal;

public record ProductFavoriteResponse(
    Long memberId,
    Long productId,
    String productName,
    String sellerBusinessName,
    String primaryImageUrl,
    BigDecimal salePrice) {}
