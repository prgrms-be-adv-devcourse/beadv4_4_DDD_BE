package com.modeunsa.boundedcontext.product.domain;

import java.util.EnumSet;
import java.util.Set;

public final class ProductPolicy {

  public static final Set<SaleStatus> DISPLAYABLE_SALE_STATUES_FOR_ALL =
      EnumSet.of(SaleStatus.SALE, SaleStatus.SOLD_OUT);

  public static final Set<ProductStatus> DISPLAYABLE_PRODUCT_STATUSES_FOR_ALL =
      EnumSet.of(ProductStatus.COMPLETED);

  public static final Set<SaleStatus> DISPLAYABLE_SALE_STATUES_FOR_SELLER =
      EnumSet.of(SaleStatus.SALE, SaleStatus.NOT_SALE, SaleStatus.SOLD_OUT);

  public static final Set<ProductStatus> DISPLAYABLE_PRODUCT_STATUES_FOR_SELLER =
      EnumSet.of(ProductStatus.DRAFT, ProductStatus.COMPLETED, ProductStatus.CANCELED);
}
