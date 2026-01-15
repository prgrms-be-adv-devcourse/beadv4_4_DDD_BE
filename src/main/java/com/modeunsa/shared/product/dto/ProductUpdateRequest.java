package com.modeunsa.shared.product.dto;

import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductUpdatableField;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductUpdateRequest implements ProductUpdatableRequest {
  private String name;
  private ProductCategory category;
  private String description;
  private BigDecimal price;
  private BigDecimal salePrice;
  private SaleStatus saleStatus;
  private Integer quantity;
  private List<String> images;

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
