package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductCreateRequest {
  @NotBlank(message = "name은 필수입니다.")
  private String name;

  private ProductCategory category;
  private String description;
  private BigDecimal price;
  private BigDecimal salePrice;
  private ProductStatus productStatus;
  private Integer quantity;
  private List<String> images;
}
