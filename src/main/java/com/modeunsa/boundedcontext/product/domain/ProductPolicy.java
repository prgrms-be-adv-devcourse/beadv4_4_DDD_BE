package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.dto.ProductUpdatableRequest;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProductPolicy {

  public static final Set<SaleStatus> DISPLAYABLE_SALE_STATUES_FOR_ALL =
      EnumSet.of(SaleStatus.SALE, SaleStatus.SOLD_OUT);

  public static final Set<ProductStatus> DISPLAYABLE_PRODUCT_STATUSES_FOR_ALL =
      EnumSet.of(ProductStatus.COMPLETED);

  public static final Set<SaleStatus> DISPLAYABLE_SALE_STATUES_FOR_SELLER =
      EnumSet.of(SaleStatus.SALE, SaleStatus.NOT_SALE, SaleStatus.SOLD_OUT);

  public static final Set<ProductStatus> DISPLAYABLE_PRODUCT_STATUES_FOR_SELLER =
      EnumSet.of(ProductStatus.DRAFT, ProductStatus.COMPLETED, ProductStatus.CANCELED);

  private static final EnumSet<ProductUpdatableField> DRAFT_ALLOWED =
      EnumSet.allOf(ProductUpdatableField.class);
  private static final EnumSet<ProductUpdatableField> COMPLETED_ALLOWED =
      EnumSet.of(
          ProductUpdatableField.DESCRIPTION,
          ProductUpdatableField.QUANTITY,
          ProductUpdatableField.IMAGES,
          ProductUpdatableField.SALE_STATUS);

  // 수정 . 허용 필드 검증
  public void validate(ProductStatus productStatus, ProductUpdatableRequest request) {
    EnumSet<ProductUpdatableField> present = request.presentFields();
    EnumSet<ProductUpdatableField> allowed =
        switch (productStatus) {
          case DRAFT -> DRAFT_ALLOWED;
          case COMPLETED -> COMPLETED_ALLOWED;
          default -> throw new GeneralException(ErrorStatus.INVALID_PRODUCT_STATE);
        };

    // 허용하지 않는 필드 체크 = present - allowed
    EnumSet<ProductUpdatableField> forbidden = EnumSet.copyOf(present);
    forbidden.removeAll(allowed);
    if (!forbidden.isEmpty()) {
      throw new GeneralException(ErrorStatus.INVALID_PRODUCT_UPDATE_FIELD);
    }
  }
}
