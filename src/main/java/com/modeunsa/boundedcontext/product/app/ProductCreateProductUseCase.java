package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductImage;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
import com.modeunsa.shared.product.dto.ProductDto;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateProductUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final EventPublisher eventPublisher;

  public Product createProduct(Long sellerId, ProductCreateRequest productCreateRequest) {
    // 판매자 검증
    ProductMemberSeller seller = productSupport.getProductMemberSeller(sellerId);

    Product product =
        Product.create(
            seller,
            productCreateRequest.getName(),
            productCreateRequest.getCategory(),
            productCreateRequest.getDescription(),
            productCreateRequest.getSalePrice(),
            productCreateRequest.getPrice(),
            productCreateRequest.getStock() != null ? productCreateRequest.getStock() : 0);

    List<String> images = productCreateRequest.getImages();
    if (images != null && !images.isEmpty()) {
      for (int i = 0; i < images.size(); i++) {
        ProductImage image = ProductImage.create(product, images.get(i), i == 0, i + 1);
        product.addImage(image);
      }
    }
    product = productRepository.save(product);
    ProductDto productDto = productMapper.toDto(product);
    eventPublisher.publish(new ProductCreatedEvent(productDto));
    return product;
  }
}
