package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.ProductCreatedEvent;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateProductUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final SpringDomainEventPublisher eventPublisher;

  public ProductResponse createProduct(ProductRequest productRequest) {
    this.validateProduct(productRequest);
    // TODO: 파일 업로드 작업 이후에 이미지 추가 예정
    Product product = productMapper.toEntity(productRequest);
    product = productRepository.save(product);

    ProductResponse response = productMapper.toResponse(product);

    eventPublisher.publish(new ProductCreatedEvent(response));

    return response;
  }

  private void validateProduct(ProductRequest productRequest) {
    // 판매자 id 없는 경우 예외 처리
    if (!productSupport.existsBySellerId(productRequest.getSellerId())) {
      throw new GeneralException(ErrorStatus.SELLER_NOT_FOUND);
    }

    if (ProductStatus.COMPLETED.equals(productRequest.getProductStatus())) {
      if (productRequest.getDescription().isBlank()) {
        throw new GeneralException(ErrorStatus.PRODUCT_DESCRIPTION_REQUIRED);
      }
      if (productRequest.getCategory() == null) {
        throw new GeneralException(ErrorStatus.PRODUCT_CATEGORY_REQUIRED);
      }
      if (productRequest.getSalePrice() == null
          || productRequest.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new GeneralException(ErrorStatus.PRODUCT_SALE_PRICE_REQUIRED);
      }
      if (productRequest.getPrice() == null
          || productRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new GeneralException(ErrorStatus.PRODUCT_PRICE_REQUIRED);
      }
      if (productRequest.getQty() <= 0) {
        throw new GeneralException(ErrorStatus.PRODUCT_QTY_REQUIRED);
      }
    }
  }
}
