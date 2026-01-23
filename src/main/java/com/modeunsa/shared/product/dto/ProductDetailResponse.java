package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductCurrency;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailResponse(
    Long id,
    Long sellerId,
    String name,
    ProductCategory category,
    String description,
    BigDecimal price,
    BigDecimal salePrice,
    ProductCurrency currency,
    ProductStatus productStatus,
    SaleStatus saleStatus,
    int stock,
    boolean isFavorite,
    int favoriteCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long createdBy,
    Long updatedBy) {}
