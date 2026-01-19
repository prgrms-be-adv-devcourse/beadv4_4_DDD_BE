package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.exception.InvalidStockException;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.eventpublisher.SpringDomainFailEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderItemDto;
import com.modeunsa.shared.product.dto.ProductStockResultDto;
import com.modeunsa.shared.product.event.ProducStockResultEvent;
import com.modeunsa.shared.product.types.ProductStockStatus;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductUpdateQuantityUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;
  private final SpringDomainEventPublisher eventPublisher;
  private final SpringDomainFailEventPublisher failEventPublisher;

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

    // 성공 여부 event 발행
    eventPublisher.publish(
        new ProducStockResultEvent(new ProductStockResultDto(ProductStockStatus.STOCK_SUCCESS)));
  }

  private void validateProducts(List<OrderItemDto> sortedItems) {
    for (OrderItemDto item : sortedItems) {
      if (item.getProductId() == null) {
        throw new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND);
      }
    }
  }

  private void decreaseProductQuantity(OrderItemDto itemDto) {
    // 재고 차감 시 비관적 락 적용
    Product product = productSupport.getProductForUpdate(itemDto.getProductId());
    try {
      product.decreaseQuantity(itemDto.getQuantity());
    } catch (InvalidStockException e) {
      failEventPublisher.publish(
          new ProducStockResultEvent(new ProductStockResultDto(ProductStockStatus.STOCK_FAILED)));
      throw new GeneralException(ErrorStatus.PRODUCT_OUT_OF_STOCK);
    }
    productRepository.save(product);
  }
}
