package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductCurrency;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductDto {
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
}
