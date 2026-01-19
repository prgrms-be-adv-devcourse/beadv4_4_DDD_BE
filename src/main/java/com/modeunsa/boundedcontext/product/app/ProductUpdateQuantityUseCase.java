package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.exception.InvalidStockException;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductUpdateQuantityUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;

  public void updateQuantity(OrderDto orderDto) {
    // 상품 ID 순으로 정렬
    List<OrderItemDto> sortedItems =
        orderDto.getOrderItems().stream()
            .sorted(Comparator.comparing(OrderItemDto::getProductId))
            .toList();

    // 상품 ID 검증
    this.validateProducts(sortedItems);

    // 재고 차감
    for (OrderItemDto item : sortedItems) {
      this.decreaseProductQuantity(item);
    }
  }

  private void validateProducts(List<OrderItemDto> sortedItems) {
    for (OrderItemDto item : sortedItems) {
      if (item.getProductId() == null) {
        throw new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND);
      }
    }
  }

  private void decreaseProductQuantity(OrderItemDto itemDto) {
    // 비관적 락
    Product product = productSupport.getProductForUpdate(itemDto.getProductId());
    try {
      product.decreaseQuantity(itemDto.getQuantity());
    } catch (InvalidStockException e) {
      throw new GeneralException(ErrorStatus.PRODUCT_OUT_OF_STOCK);
    }
    productRepository.save(product);
  }
}
