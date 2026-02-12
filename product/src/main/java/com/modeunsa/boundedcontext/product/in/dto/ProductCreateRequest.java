package com.modeunsa.boundedcontext.product.in.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
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
  @PositiveOrZero private BigDecimal price;
  @PositiveOrZero private BigDecimal salePrice;
  private List<String> images;
}
