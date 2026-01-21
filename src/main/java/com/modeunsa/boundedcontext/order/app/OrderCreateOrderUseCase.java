package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.order.event.OrderCreatedEvent;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductOrderValidateRequest;
import com.modeunsa.shared.product.dto.ProductStockResponse;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest.ProductOrderItemDto;
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
  private final SpringDomainEventPublisher eventPublisher;
  private final ProductApiClient productApiClient;

  public OrderResponseDto createOrder(Long memberId, CreateOrderRequestDto requestDto) {
    // 회원 확인
    OrderMember member = orderSupport.findByMemberId(memberId);

    // 상품 조회 및 검증
    ProductOrderResponse product =
        getValidatedProduct(requestDto.getProductId(), requestDto.getQuantity());

    // 주문 생성 및 저장
    Order order = createAndSaveOrder(member, product, requestDto);

    // 재고 차감 및 검증
    requestDecreaseStock(order);

    eventPublisher.publish(new OrderCreatedEvent(orderMapper.toOrderDto(order)));

    return orderMapper.toOrderResponseDto(order);
  }

  // --- Private Methods] ---

  // 상품 정보 조회
  private ProductOrderResponse getValidatedProduct(Long productId, int quantity) {
    // 상품 모듈 api 호출
    List<ProductOrderResponse> responses =
        productApiClient.validateOrderProducts(new ProductOrderValidateRequest(List.of(productId)));

    ProductOrderResponse product =
        responses.stream()
            .filter(p -> p.productId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND));

    // 검증 (품절, 재고 부족)
    if (product.stock()
        < quantity) { // !product.isAvailable() TODO: 상품 상태필드 받고 검증 추가, 지금 request에 상태 없음. 그리고 상품 새로
      // 추가될 때 판매중상태 아님.
      throw new GeneralException(ErrorStatus.ORDER_STOCK_NOT_ENOUGH, List.of(productId));
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
  private void requestDecreaseStock(Order order) {
    // 변환
    List<ProductOrderItemDto> items =
        order.getOrderItems().stream()
            .map(item -> new ProductOrderItemDto(item.getProductId(), item.getQuantity()))
            .toList();

    // API 호출
    List<ProductStockResponse> stockResult =
        productApiClient.updateStock(new ProductStockUpdateRequest(order.getId(), items));

    List<Long> failedProductIds =
        stockResult.stream()
            .filter(result -> !result.success())
            .map(ProductStockResponse::productId)
            .toList();

    // 실패한 게 하나라도 있으면 예외 발생 (ID 목록 함께 전달)
    if (!failedProductIds.isEmpty()) {
      throw new GeneralException(ErrorStatus.ORDER_STOCK_NOT_ENOUGH, failedProductIds);
    }
  }
}
