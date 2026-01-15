package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
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

  public ProductResponse createProduct(Long sellerId, ProductCreateRequest productCreateRequest) {
    this.validateProduct(sellerId, productCreateRequest);
    ProductMemberSeller seller = productSupport.getProductMemberSeller(sellerId);
    // TODO: 파일 업로드 작업 이후에 이미지 추가 예정
    Product product = Product.create(seller, productCreateRequest);
    product = productRepository.save(product);
    ProductResponse response = productMapper.toResponse(product);
    eventPublisher.publish(new ProductCreatedEvent(response));

    return response;
  }

  private void validateProduct(Long sellerId, ProductCreateRequest productCreateRequest) {
    // 판매자 id 없는 경우 예외 처리
    if (!productSupport.existsBySellerId(sellerId)) {
      throw new GeneralException(ErrorStatus.SELLER_NOT_FOUND);
    }

    if (ProductStatus.COMPLETED.equals(productCreateRequest.getProductStatus())) {
      if (productCreateRequest.getDescription().isBlank()) {
        throw new GeneralException(ErrorStatus.PRODUCT_DESCRIPTION_REQUIRED);
      }
      if (productCreateRequest.getCategory() == null) {
        throw new GeneralException(ErrorStatus.PRODUCT_CATEGORY_REQUIRED);
      }
      if (productCreateRequest.getSalePrice() == null
          || productCreateRequest.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new GeneralException(ErrorStatus.PRODUCT_SALE_PRICE_REQUIRED);
      }
      if (productCreateRequest.getPrice() == null
          || productCreateRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new GeneralException(ErrorStatus.PRODUCT_PRICE_REQUIRED);
      }
      if (productCreateRequest.getQuantity() == null || productCreateRequest.getQuantity() <= 0) {
        throw new GeneralException(ErrorStatus.PRODUCT_QTY_REQUIRED);
      }
    }
  }
}
