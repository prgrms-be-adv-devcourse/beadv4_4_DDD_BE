package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.ProductCreatedEvent;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
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
    // 판매자 id 없는 경우 예외 처리
    if (!productSupport.existsBySellerId(productRequest.getSellerId())) {
      throw new GeneralException(ErrorStatus.SELLER_NOT_FOUND);
    }
    ;

    // TODO: 파일 업로드 작업 이후에 이미지 추가 예정
    Product product = productMapper.toEntity(productRequest);
    product = productRepository.save(product);

    ProductResponse response = productMapper.toResponse(product);

    eventPublisher.publish(new ProductCreatedEvent(response));

    return response;
  }
}
