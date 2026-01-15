package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.dto.ProductResponse;
import com.modeunsa.shared.product.dto.ProductUpdatableRequest;
import com.modeunsa.shared.product.dto.ProductUpdateRequest;
import com.modeunsa.shared.product.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductUpdateProductUseCase {

  private final ProductRepository productRepository;
  private final ProductSupport productSupport;
  private final ProductMapper productMapper;
  private final SpringDomainEventPublisher eventPublisher;
  private final ProductPolicy productPolicy;

  public ProductResponse updateProduct(
      Long sellerId, Long productId, ProductUpdateRequest request) {
    Product product = productSupport.getProduct(productId);
    // 업데이트 정책 검증
    this.validateProduct(sellerId, request);
    productPolicy.validate(product.getProductStatus(), request);

    product.update(request);

    ProductResponse productResponse = productMapper.toResponse(productRepository.save(product));

    eventPublisher.publish(new ProductUpdatedEvent(productResponse));

    return productResponse;
  }

  private void validateProduct(Long sellerId, ProductUpdatableRequest request) {
    // 1. 판매자 검증
    if (sellerId == null || !productSupport.existsBySellerId(sellerId)) {
      throw new GeneralException(ErrorStatus.SELLER_NOT_FOUND);
    }

    // 2. 공통 숫자 검증
    if (request.getPrice() != null && request.getPrice().signum() < 0) {
      throw new GeneralException(ErrorStatus.PRODUCT_PRICE_REQUIRED);
    }

    if (request.getSalePrice() != null && request.getSalePrice().signum() < 0) {
      throw new GeneralException(ErrorStatus.PRODUCT_SALE_PRICE_REQUIRED);
    }

    if (request.getQuantity() != null && request.getQuantity() < 0) {
      throw new GeneralException(ErrorStatus.PRODUCT_QTY_REQUIRED);
    }
  }
}
