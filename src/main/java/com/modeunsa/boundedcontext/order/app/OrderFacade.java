package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderListResponseDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderFacade {
  private final OrderCreateCartItemUsecase orderCreateCartItemUsecase;
  private final OrderSupport orderSupport;
  private final OrderCreateOrderUsecase orderCreateOrderUseCase;
  private final OrderGetOrdersUseCase orderGetOrdersUseCase;
  private final OrderCancelOrderUseCase orderCancelOrderUseCase;

  // 장바구니 아이템 생성
  @Transactional
  public CreateCartItemResponseDto createCartItem(
      long memberId, CreateCartItemRequestDto requestDto) {
    return orderCreateCartItemUsecase.createCartItem(memberId, requestDto);
  }

  public long countProduct() {
    return orderSupport.countProduct();
  }

  public long countMember() {
    return orderSupport.countMember();
  }

  public OrderMember findByMemberId(long memberId) {
    return orderSupport.findByMemberId(memberId);
  }

  public OrderProduct findByProductId(long productId) {
    return orderSupport.findByProductId(productId);
  }

  // 단건 주문 생성
  @Transactional
  public OrderResponseDto createOrder(long memberId, CreateOrderRequestDto requestDto) {
    return orderCreateOrderUseCase.createOrder(memberId, requestDto);
  }

  public long countOrder() {
    return orderSupport.countOrder();
  }

  public Page<OrderListResponseDto> getOrders(long memberId, Pageable pageable) {
    return orderGetOrdersUseCase.getOrders(memberId, pageable);
  }

  // 주문 취소 요청
  @Transactional
  public OrderResponseDto cancelOrder(long memberId, Long orderId) {
    return orderCancelOrderUseCase.cancelOrder(memberId, orderId);
  }
}
