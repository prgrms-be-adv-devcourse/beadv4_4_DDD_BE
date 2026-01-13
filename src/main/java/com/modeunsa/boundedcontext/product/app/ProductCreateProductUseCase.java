package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.shared.product.ProductCreatedEvent;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateProductUseCase {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final SpringDomainEventPublisher eventPublisher;

  public ProductResponse createProduct(ProductRequest productRequest) {
    Product product = productMapper.toEntity(productRequest);
    // TODO: 파일 업로드 작업 이후에 이미지 추가 예정
    product = productRepository.save(product);
    ProductResponse response = productMapper.toResponse(product);
    eventPublisher.publish(new ProductCreatedEvent(response));
    return response;
  }
}
