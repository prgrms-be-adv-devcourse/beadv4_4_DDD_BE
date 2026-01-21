package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRestoreStockUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;

  public void restoreStock(OrderDto orderDto) {
    // 데드락 발생 방지를 위해 상품 ID 순으로 정렬
    List<OrderItemDto> sortedItems =
        orderDto.getOrderItems().stream()
            .sorted(Comparator.comparing(OrderItemDto::getProductId))
            .toList();

    // 상품 ID 검증
    productSupport.validateProducts(sortedItems.stream().map(OrderItemDto::getProductId).toList());

    // 상품 재고 원복
    for (OrderItemDto item : sortedItems) {
      this.increaseStock(item);
    }
  }

  private void increaseStock(OrderItemDto dto) {
    try {
      Product product = productSupport.getProductForUpdate(dto.getProductId());
      product.increaseStock(dto.getQuantity());
      productRepository.save(product);
    } catch (PessimisticLockingFailureException e) {
      // 타임아웃을 포함한 락 획득 실패
      throw new GeneralException(ErrorStatus.PRODUCT_STOCK_LOCK_FAILURE);
    }
  }
}
