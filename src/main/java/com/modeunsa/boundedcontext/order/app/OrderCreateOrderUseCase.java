package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.inventory.out.InventoryApiClient;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.order.event.OrderCreatedEvent;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductOrderValidateRequest;
import com.modeunsa.shared.product.out.ProductApiClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateOrderUseCase {
  private final OrderSupport orderSupport;
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final EventPublisher eventPublisher;
  private final ProductApiClient productApiClient;
  private final InventoryApiClient inventoryApiClient;

  public OrderResponseDto createOrder(Long memberId, CreateOrderRequestDto requestDto) {
    // 회원 확인
    OrderMember member = orderSupport.findByMemberId(memberId);

    // 상품 조회 및 검증
    ProductOrderResponse product = getValidatedProduct(requestDto.getProductId());

    // 주문 생성 및 저장
    Order order = createAndSaveOrder(member, product, requestDto);

    // 재고 차감 및 검증
    requestReserveInventory(order);

    eventPublisher.publish(new OrderCreatedEvent(orderMapper.toOrderDto(order)));

    return orderMapper.toOrderResponseDto(order);
  }

  // --- Private Methods] ---

  // 상품 정보 조회
  private ProductOrderResponse getValidatedProduct(Long productId) {
    // 상품 모듈 api 호출
    List<ProductOrderResponse> responses =
        productApiClient.validateOrderProducts(new ProductOrderValidateRequest(List.of(productId)));

    ProductOrderResponse product =
        responses.stream()
            .filter(p -> p.productId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND));

    if (!product.isAvailable()) {
      throw new GeneralException(ErrorStatus.PRODUCT_NOT_ON_SALE);
    }

    return product;
  }

  // 주문 생성 및 저장
  private Order createAndSaveOrder(
      OrderMember member, ProductOrderResponse product, CreateOrderRequestDto request) {
    OrderItem orderItem = orderMapper.toOrderItemEntity(product, request);

    Order order =
        Order.createOrder(
            member,
            List.of(orderItem),
            request.getRecipientName(),
            request.getRecipientPhone(),
            request.getZipCode(),
            request.getAddress(),
            request.getAddressDetail());

    return orderRepository.save(order);
  }

  // 상품 재고 차감
  private void requestReserveInventory(Order order) {
    // 변환
    List<InventoryReserveRequest.Item> items =
        order.getOrderItems().stream()
            .map(item -> new InventoryReserveRequest.Item(item.getProductId(), item.getQuantity()))
            .toList();

    // API 호출
    inventoryApiClient.reserveInventory(new InventoryReserveRequest(items));
  }
}
