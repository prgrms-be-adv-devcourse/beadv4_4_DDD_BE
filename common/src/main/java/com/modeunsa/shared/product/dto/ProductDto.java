package com.modeunsa.shared.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ProductDto {
  private Long id;
  private Long sellerId;
  private String sellerBusinessName;
  private String name;
  private BigDecimal price;
  private BigDecimal salePrice;
  private String primaryImageUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long createdBy;
  private Long updatedBy;
}
