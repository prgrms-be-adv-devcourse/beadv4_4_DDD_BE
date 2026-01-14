package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductCurrency;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductRequest {
  // TODO: @NotNull(message = "sellerId는 필수입니다.")
  private Long sellerId;

  @NotBlank(message = "name은 필수입니다.")
  private String name;

  private ProductCategory category;
  private String description;
  private BigDecimal price;
  private BigDecimal salePrice;
  private ProductCurrency currency;
  private ProductStatus productStatus;
  private SaleStatus saleStatus;
  private int qty;
}
