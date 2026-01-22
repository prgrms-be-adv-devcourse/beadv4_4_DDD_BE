package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.exception.InvalidStockException;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.shared.product.dto.ProductStockDto;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest.ProductOrderItemDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDeductStockUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;

  public List<ProductStockDto> deductStock(ProductStockUpdateRequest request) {
    // 데드락 발생 방지를 위해 상품 ID 순으로 재정렬
    List<ProductOrderItemDto> sortedItems =
        request.items().stream()
            .sorted(Comparator.comparing(ProductOrderItemDto::productId))
            .toList();

    // 상품 ID 검증
    productSupport.validateProducts(
        sortedItems.stream().map(ProductOrderItemDto::productId).toList());

    // 재고 차감
    List<ProductStockDto> products = new ArrayList<>();
    for (ProductOrderItemDto item : sortedItems) {
      products.add(this.decreaseStock(item));
    }

    return products;
  }

  private ProductStockDto decreaseStock(ProductOrderItemDto itemDto) {
    // 재고 차감 시 비관적 락 적용
    Product product = productSupport.getProductForUpdate(itemDto.productId());
    boolean success;
    try {
      product.decreaseStock(itemDto.quantity());
      success = true;
    } catch (InvalidStockException e) {
      success = false;
    }
    // 재고 0인 경우 솔드아웃 처리
    if (product.isSoldOut()) {
      product.setSoldOut();
    }
    productRepository.save(product);
    return new ProductStockDto(product.getId(), success, product.getStock());
  }
}
