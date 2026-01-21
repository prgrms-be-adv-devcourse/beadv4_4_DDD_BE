package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.CreateCartOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.order.event.OrderCreatedEvent;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductOrderValidateRequest;
import com.modeunsa.shared.product.dto.ProductStockResponse;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest.ProductOrderItemDto;
import com.modeunsa.shared.product.out.ProductApiClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateCartOrderUseCase {
  private final OrderSupport orderSupport;
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final SpringDomainEventPublisher eventPublisher;
  private final ProductApiClient productApiClient;

  public OrderResponseDto createCartOrder(Long memberId, CreateCartOrderRequestDto requestDto) {
    // 회원 및 장바구니 목록 조회
    OrderMember member = orderSupport.findByMemberId(memberId);
    List<CartItem> cartItems = orderSupport.getCartItemsByMemberId(memberId);

    if (cartItems.isEmpty()) {
      throw new GeneralException(ErrorStatus.ORDER_CART_EMPTY);
    }

    // 상품 정보 일괄 조회 및 재고 검증
    Map<Long, ProductOrderResponse> productMap = getValidatedProductsMap(cartItems);

    // 주문 생성 및 저장
    Order order = createAndSaveOrder(member, cartItems, productMap, requestDto);

    // 재고 일괄 차감 요청
    processStockDecrease(order);

    // 장바구니 비우기
    orderSupport.clearCart(memberId);

    eventPublisher.publish(new OrderCreatedEvent(orderMapper.toOrderDto(order)));

    return orderMapper.toOrderResponseDto(order);
  }

  // --- [Private Methods] ---

  // 상품 검증 및 변환
  private Map<Long, ProductOrderResponse> getValidatedProductsMap(List<CartItem> cartItems) {
    // 장바구니에 있는 모든 상품 ID 추출
    List<Long> productIds = cartItems.stream().map(CartItem::getProductId).toList();

    // 상품 모듈 API 일괄 호출
    List<ProductOrderResponse> responses =
        productApiClient.validateOrderProducts(new ProductOrderValidateRequest(productIds));

    // Map<ItemId, ProductResponse>으로 변환
    Map<Long, ProductOrderResponse> productMap =
        responses.stream().collect(Collectors.toMap(ProductOrderResponse::productId, p -> p));

    // 검증 로직
    for (CartItem item : cartItems) {
      ProductOrderResponse product = productMap.get(item.getProductId());

      // 1) 상품이 조회되지 않음
      if (product == null) {
        throw new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND);
      }
      // 2) 재고 부족
      if (product.stock() < item.getQuantity()) {
        throw new GeneralException(
            ErrorStatus.ORDER_STOCK_NOT_ENOUGH, List.of(item.getProductId()));
      }
    }

    return productMap;
  }

  // 주문 생성 및 저장
  private Order createAndSaveOrder(
      OrderMember member,
      List<CartItem> cartItems,
      Map<Long, ProductOrderResponse> productMap,
      CreateCartOrderRequestDto request) {

    List<OrderItem> orderItems = new ArrayList<>();

    for (CartItem cartItem : cartItems) {
      ProductOrderResponse productInfo = productMap.get(cartItem.getProductId());

      // 실시간 조회된 정보로 OrderItem 생성
      OrderItem orderItem = orderMapper.toOrderItem(productInfo, cartItem.getQuantity());
      orderItems.add(orderItem);
    }

    Order order =
        Order.createOrder(
            member,
            orderItems,
            request.getRecipientName(),
            request.getRecipientPhone(),
            request.getZipCode(),
            request.getAddress(),
            request.getAddressDetail());

    return orderRepository.save(order);
  }

  // 재고 차감
  private void processStockDecrease(Order order) {
    // DTO 변환
    List<ProductOrderItemDto> items =
        order.getOrderItems().stream()
            .map(item -> new ProductOrderItemDto(item.getProductId(), item.getQuantity()))
            .toList();

    // API 호출
    List<ProductStockResponse> stockResults =
        productApiClient.updateStock(new ProductStockUpdateRequest(order.getId(), items));

    // 실패 건 필터링
    List<Long> failedProductIds =
        stockResults.stream()
            .filter(result -> !result.success())
            .map(ProductStockResponse::productId)
            .toList();

    if (!failedProductIds.isEmpty()) {
      throw new GeneralException(ErrorStatus.ORDER_STOCK_NOT_ENOUGH, failedProductIds);
    }
  }
}
