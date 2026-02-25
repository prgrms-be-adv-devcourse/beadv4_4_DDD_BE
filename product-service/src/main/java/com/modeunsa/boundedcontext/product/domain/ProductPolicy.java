package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.boundedcontext.product.in.dto.ProductUpdatableRequest;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProductPolicy {

  public static final Set<SaleStatus> ORDERABLE_SALE_STATUES = EnumSet.of(SaleStatus.SALE);

  public static final Set<ProductStatus> ORDERABLE_PRODUCT_STATUES =
      EnumSet.of(ProductStatus.COMPLETED);

  public static final Set<ProductCategory> BEAUTY_CATEGORIES = EnumSet.of(ProductCategory.BEAUTY);

  private static final EnumSet<ProductUpdatableField> DRAFT_ALLOWED =
      EnumSet.allOf(ProductUpdatableField.class);
  private static final EnumSet<ProductUpdatableField> COMPLETED_ALLOWED =
      EnumSet.of(
          ProductUpdatableField.DESCRIPTION,
          ProductUpdatableField.IMAGES,
          ProductUpdatableField.SALE_STATUS);

  private static final EnumSet<ProductStatus> DRAFT_ALLOWED_STATUSES =
      EnumSet.of(ProductStatus.COMPLETED, ProductStatus.CANCELED);

  public static boolean isOrderable(SaleStatus saleStatus, ProductStatus productStatus) {
    return ORDERABLE_SALE_STATUES.contains(saleStatus)
        && ORDERABLE_PRODUCT_STATUES.contains(productStatus);
  }

  public void validate(ProductStatus productStatus, ProductUpdatableRequest request) {
    // 1. 수정 가능 필드 검증
    EnumSet<ProductUpdatableField> present = request.presentFields();
    EnumSet<ProductUpdatableField> allowed =
        switch (productStatus) {
          case DRAFT -> DRAFT_ALLOWED;
          case COMPLETED -> COMPLETED_ALLOWED;
          default -> throw new GeneralException(ErrorStatus.INVALID_PRODUCT_STATUS);
        };

    // 허용하지 않는 필드 체크 = present - allowed
    EnumSet<ProductUpdatableField> forbidden = EnumSet.copyOf(present);
    forbidden.removeAll(allowed);
    if (!forbidden.isEmpty()) {
      throw new GeneralException(ErrorStatus.INVALID_PRODUCT_UPDATE_FIELD);
    }
  }

  public void validateProductStatus(ProductStatus oldStatus, ProductStatus newStatus) {
    // 1. 임시저장 -> 완료 / 취소
    if (ProductStatus.DRAFT.equals(oldStatus) && !DRAFT_ALLOWED_STATUSES.contains(newStatus)) {
      throw new GeneralException(ErrorStatus.INVALID_PRODUCT_STATUS);
    }

    // 2. 완료 & 취소 -> no action
    if (ProductStatus.COMPLETED.equals(oldStatus) || ProductStatus.CANCELED.equals(oldStatus)) {
      throw new GeneralException(ErrorStatus.INVALID_PRODUCT_STATUS);
    }
  }
}
