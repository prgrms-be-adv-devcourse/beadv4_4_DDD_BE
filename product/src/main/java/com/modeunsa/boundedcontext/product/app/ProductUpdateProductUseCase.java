package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductImage;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import com.modeunsa.boundedcontext.product.in.dto.ProductUpdateRequest;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.product.dto.ProductDto;
import com.modeunsa.shared.product.dto.ProductOrderAvailableDto;
import com.modeunsa.shared.product.event.ProductOrderAvailabilityChangedEvent;
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
  private final EventPublisher eventPublisher;
  private final ProductPolicy productPolicy;

  public Product updateProduct(Long sellerId, Long productId, ProductUpdateRequest request) {
    // 1. 판매자 및 상품 검증
    ProductMemberSeller seller = productSupport.getProductMemberSeller(sellerId);
    Product product = productSupport.getProduct(productId, seller.getId());

    // 2. 정책 검증
    productPolicy.validate(product.getProductStatus(), request);

    // 3. 이미지 업데이트
    List<String> images = request.getImages();
    product.clearImages();
    if (images != null && !images.isEmpty()) {
      for (int i = 0; i < images.size(); i++) {
        ProductImage image = ProductImage.create(product, images.get(i), i == 0, i + 1);
        product.addImage(image);
      }
    }

    // 4. 주문 가능 여부 이벤트 발행 여부 확인
    // TODO: 상품 수정 시 saleStatus 필수값 지정
    boolean oldAvailable = product.isOrderAvailable();
    boolean newAvailable =
        checkIfOrderAvailable(product.getProductStatus(), request.getSaleStatus());
    boolean isAvailableChanged = oldAvailable != newAvailable;

    // 5. 상품 업데이트
    product.update(
        request.getName(),
        request.getCategory(),
        request.getDescription(),
        request.getSaleStatus(),
        request.getPrice(),
        request.getSalePrice());

    ProductDto productDto = productMapper.toDto(productRepository.save(product));

    // 6. 이벤트 발행 (상품 업데이트, 주문 가능 여부 업데이트)
    eventPublisher.publish(new ProductUpdatedEvent(productDto));
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
