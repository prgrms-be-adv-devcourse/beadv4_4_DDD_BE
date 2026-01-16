package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.dto.ProductDto;
import com.modeunsa.shared.product.event.ProductStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductUpdateProductStatusUseCase {

  private final ProductRepository productRepository;
  private final ProductSupport productSupport;
  private final ProductMapper productMapper;
  private final SpringDomainEventPublisher eventPublisher;
  private final ProductPolicy productPolicy;

  public Product updateProductStatus(Long sellerId, Long productId, ProductStatus productStatus) {
    Product product = productSupport.getProduct(productId);

    // 1. 판매자 검증
    this.validateSeller(sellerId, product.getSeller().getId());

    // 2. 정책 검증
    productPolicy.validateProductStatus(product.getProductStatus(), productStatus);

    product.updateProductStatus(productStatus);

    ProductDto productDto = productMapper.toDto(productRepository.save(product));

    eventPublisher.publish(new ProductStatusUpdatedEvent(productDto));

    return product;
  }

  private void validateSeller(Long sellerId, Long productSellerId) {
    // 판매자 존재 여부 확인
    if (sellerId == null || !productSupport.existsBySellerId(sellerId)) {
      throw new GeneralException(ErrorStatus.SELLER_NOT_FOUND);
    }
    // 판매자 일치 여부 확인
    if (!sellerId.equals(productSellerId)) {
      throw new GeneralException(ErrorStatus.INVALID_PRODUCT_MEMBER);
    }
  }
}
