package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductImage;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.product.dto.ProductDto;
import com.modeunsa.shared.product.dto.ProductUpdateRequest;
import com.modeunsa.shared.product.event.ProductUpdatedEvent;
import java.util.List;
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

  public Product updateProduct(Long sellerId, Long productId, ProductUpdateRequest request) {
    // 판매자 검증
    if (sellerId == null || !productSupport.existsBySellerId(sellerId)) {
      throw new GeneralException(ErrorStatus.SELLER_NOT_FOUND);
    }

    Product product = productSupport.getProduct(productId);

    // 정책 검증
    productPolicy.validate(product.getProductStatus(), request);

    List<String> images = request.getImages();
    if (images != null && !images.isEmpty()) {
      product.clearImages();
      for (int i = 0; i < images.size(); i++) {
        ProductImage image = ProductImage.create(product, images.get(i), i == 0, i + 1);
        product.addImage(image);
      }
    }

    product.update(
        request.getName(),
        request.getCategory(),
        request.getDescription(),
        request.getSaleStatus(),
        request.getPrice(),
        request.getSalePrice(),
        request.getStock());

    ProductDto productDto = productMapper.toDto(productRepository.save(product));

    eventPublisher.publish(new ProductUpdatedEvent(productDto));

    return product;
  }
}
