package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductCurrency;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ProductResponse {
  private long id;
  private long sellerId;
  private String name;
  private ProductCategory category;
  private String description;
  private int price;
  private int salePrice;
  private ProductCurrency currency;
  private ProductStatus productStatus;
  private SaleStatus saleStatus;
  private int qty;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long createdBy;
  private Long updatedBy;
}
