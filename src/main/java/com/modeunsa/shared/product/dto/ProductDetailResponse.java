package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductCurrency;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductDetailResponse {
  private Long id;
  private Long sellerId;
  private String name;
  private ProductCategory category;
  private String description;
  private BigDecimal price;
  private BigDecimal salePrice;
  private ProductCurrency currency;
  private ProductStatus productStatus;
  private SaleStatus saleStatus;
  private int quantity;
  private Boolean isFavorite;
  private int favoriteCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long createdBy;
  private Long updatedBy;
}
