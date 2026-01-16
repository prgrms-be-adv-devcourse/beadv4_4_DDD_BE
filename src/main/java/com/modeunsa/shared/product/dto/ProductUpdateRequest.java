package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductUpdatableField;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductUpdateRequest implements ProductUpdatableRequest {
  private final String name;
  private final ProductCategory category;
  private final String description;
  @PositiveOrZero private final BigDecimal price;
  @PositiveOrZero private final BigDecimal salePrice;
  private final SaleStatus saleStatus;
  @PositiveOrZero private final Integer quantity;
  private final List<String> images;

  @Override
  public EnumSet<ProductUpdatableField> presentFields() {
    EnumSet<ProductUpdatableField> set = EnumSet.noneOf(ProductUpdatableField.class);
    if (name != null) {
      set.add(ProductUpdatableField.NAME);
    }
    if (category != null) {
      set.add(ProductUpdatableField.CATEGORY);
    }
    if (description != null) {
      set.add(ProductUpdatableField.DESCRIPTION);
    }
    if (price != null) {
      set.add(ProductUpdatableField.PRICE);
    }
    if (salePrice != null) {
      set.add(ProductUpdatableField.SALE_PRICE);
    }
    if (saleStatus != null) {
      set.add(ProductUpdatableField.SALE_STATUS);
    }
    if (quantity != null) {
      set.add(ProductUpdatableField.QUANTITY);
    }
    if (images != null) {
      set.add(ProductUpdatableField.IMAGES);
    }
    return set;
  }
}
