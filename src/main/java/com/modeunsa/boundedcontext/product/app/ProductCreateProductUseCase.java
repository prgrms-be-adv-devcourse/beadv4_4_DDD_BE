package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateProductUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final SpringDomainEventPublisher eventPublisher;

  public Product createProduct(Long sellerId, ProductCreateRequest productCreateRequest) {
    // 판매자 검증
    if (sellerId == null || !productSupport.existsBySellerId(sellerId)) {
      throw new GeneralException(ErrorStatus.PRODUCT_SELLER_NOT_FOUND);
    }
    ProductMemberSeller seller = productSupport.getProductMemberSeller(sellerId);
    // TODO: 파일 업로드 작업 이후에 이미지 추가 예정
    Product product =
        Product.create(
            seller,
            productCreateRequest.getName(),
            productCreateRequest.getCategory(),
            productCreateRequest.getDescription(),
            productCreateRequest.getQuantity());
    product = productRepository.save(product);
    eventPublisher.publish(new ProductCreatedEvent(productMapper.toDto(product)));
    return product;
  }
}
