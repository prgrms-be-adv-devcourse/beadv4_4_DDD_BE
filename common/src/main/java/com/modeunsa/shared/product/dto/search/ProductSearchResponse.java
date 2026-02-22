package com.modeunsa.shared.product.dto.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductSearchResponse(
    String id,
    String name,
    String sellerBusinessName,
    String category,
    BigDecimal salePrice,
    String primaryImageUrl,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime createdAt) {}
