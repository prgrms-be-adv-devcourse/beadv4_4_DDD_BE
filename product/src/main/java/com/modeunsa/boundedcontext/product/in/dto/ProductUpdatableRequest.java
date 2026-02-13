package com.modeunsa.boundedcontext.product.in.dto;

import com.modeunsa.boundedcontext.product.domain.ProductUpdatableField;
import java.math.BigDecimal;
import java.util.EnumSet;

public interface ProductUpdatableRequest {

  EnumSet<ProductUpdatableField> presentFields();

  BigDecimal getPrice();

  BigDecimal getSalePrice();
}
