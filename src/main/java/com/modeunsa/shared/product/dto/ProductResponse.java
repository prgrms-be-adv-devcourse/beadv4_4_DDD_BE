package com.modeunsa.shared.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductCurrency;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    Long sellerId,
    String sellerBusinessName,
    String name,
    ProductCategory category,
    String description,
    BigDecimal price,
    BigDecimal salePrice,
    ProductCurrency currency,
    ProductStatus productStatus,
    SaleStatus saleStatus,
    int stock,
    int favoriteCount,
    String primaryImageUrl,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul") LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul") LocalDateTime updatedAt,
    Long createdBy,
    Long updatedBy) {}
