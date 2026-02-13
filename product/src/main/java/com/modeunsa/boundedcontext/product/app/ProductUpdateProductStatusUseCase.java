package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.product.dto.ProductOrderAvailableDto;
import com.modeunsa.shared.product.event.ProductOrderAvailabilityChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductUpdateProductStatusUseCase {

  private final ProductSupport productSupport;
  private final ProductPolicy productPolicy;
  private final EventPublisher eventPublisher;

  public Product updateProductStatus(Long sellerId, Long productId, ProductStatus productStatus) {
    // 1. 판매자 및 상품 검증
    ProductMemberSeller seller = productSupport.getProductMemberSeller(sellerId);
    Product product = productSupport.getProduct(productId, seller.getId());

    // 2. 정책 검증
    productPolicy.validateProductStatus(product.getProductStatus(), productStatus);

    // 3. 이벤트 발행 여부 확인
    boolean oldAvailable = product.isOrderAvailable();
    boolean newAvailable = checkIfOrderAvailable(productStatus, product.getSaleStatus());
    boolean isAvailableChanged = oldAvailable != newAvailable;

    // 4. 상태 업데이트
    product.updateProductStatus(productStatus);

    // 5. 이벤트 발행
    if (isAvailableChanged) {
      eventPublisher.publish(
          new ProductOrderAvailabilityChangedEvent(
              new ProductOrderAvailableDto(product.getId(), newAvailable)));
    }
    return product;
  }

  private boolean checkIfOrderAvailable(ProductStatus productStatus, SaleStatus newSaleStatus) {
    return ProductPolicy.ORDERABLE_PRODUCT_STATUES.contains(productStatus)
        && ProductPolicy.ORDERABLE_SALE_STATUES.contains(newSaleStatus);
  }
}
