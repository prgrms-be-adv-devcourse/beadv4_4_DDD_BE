package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.shared.order.dto.CartItemsResponseDto;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import com.modeunsa.shared.order.dto.CreateCartOrderRequestDto;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderListResponseDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderFacade {
  private final OrderCreateCartItemUseCase orderCreateCartItemUseCase;
  private final OrderSupport orderSupport;
  private final OrderCreateOrderUseCase orderCreateOrderUseCase;
  private final OrderGetOrdersUseCase orderGetOrdersUseCase;
  private final OrderCancelOrderUseCase orderCancelOrderUseCase;
  private final OrderCreateCartOrderUseCase orderCreateCartOrderUseCase;
  private final OrderRefundOrderUseCase orderRefundOrderUseCase;
  private final OrderGetCartItemsUseCase orderGetCartItemsUseCase;
  private final OrderMapper orderMapper;
  private final OrderSyncMemberUseCase orderSyncMemberUseCase;
  private final OrderUpdateMemberUseCase orderUpdateMemberUseCase;
  private final OrderCreateDeliveryAddress orderCreateDeliveryAddress;

  // 장바구니 아이템 생성
  @Transactional
  public CreateCartItemResponseDto createCartItem(
      Long memberId, CreateCartItemRequestDto requestDto) {
    return orderCreateCartItemUseCase.createCartItem(memberId, requestDto);
  }

  public long countProduct() {
    return orderSupport.countProduct();
  }

  public long countMember() {
    return orderSupport.countMember();
  }

  public OrderMember findByMemberId(Long memberId) {
    return orderSupport.findByMemberId(memberId);
  }

  public OrderProduct findByProductId(Long productId) {
    return orderSupport.findByProductId(productId);
  }

  // 단건 주문 생성
  @Transactional
  public OrderResponseDto createOrder(Long memberId, CreateOrderRequestDto requestDto) {
    return orderCreateOrderUseCase.createOrder(memberId, requestDto);
  }

  // 장바구니 주문 생성
  @Transactional
  public OrderResponseDto createCartOrder(Long memberId, CreateCartOrderRequestDto requestDto) {
    return orderCreateCartOrderUseCase.createCartOrder(memberId, requestDto);
  }

  public long countOrder() {
    return orderSupport.countOrder();
  }

  public Page<OrderListResponseDto> getOrders(Long memberId, Pageable pageable) {
    return orderGetOrdersUseCase.getOrders(memberId, pageable);
  }

  // 주문 취소 요청
  @Transactional
  public OrderResponseDto cancelOrder(Long memberId, Long orderId) {
    return orderCancelOrderUseCase.cancelOrder(memberId, orderId);
  }

  // 주문 취소 요청
  @Transactional
  public OrderResponseDto refundOrder(Long memberId, Long orderId) {
    return orderRefundOrderUseCase.refundOrder(memberId, orderId);
  }

  // 장바구니 상품 목록 조회
  public CartItemsResponseDto getCartItems(long memberId) {
    return orderGetCartItemsUseCase.getCartItems(memberId);
  }

  public OrderDto getOrder(Long id) {
    Order order = orderSupport.findByOrderId(id);
    return orderMapper.toOrderDto(order);
  }

  // ---- sync ----
  @Transactional
  public void createProduct(ProductDto productDto) {
    OrderProduct product = orderMapper.toOrderProduct(productDto);
    orderSupport.saveProduct(product);
  }

  @Transactional
  public void updateProduct(ProductDto productDto) {
    OrderProduct orderProduct = orderSupport.findByProductId(productDto.getId());
    orderMapper.updateFromProductDto(productDto, orderProduct);
  }

  @Transactional
  public void approveOrder(PaymentDto payment) {
    Order order = orderSupport.findByOrderId(payment.orderId());
    order.approve();
  }

  @Transactional
  public void rejectOrder(PaymentDto payment) {
    Order order = orderSupport.findByOrderId(payment.orderId());
    order.reject();
  }

  @Transactional
  public void syncMember(Long memberId, String memberName, String memberPhone) {
    orderSyncMemberUseCase.syncMember(memberId, memberName, memberPhone);
  }

  @Transactional
  public void updateMember(Long memberId, String memberName, String memberPhone) {
    orderUpdateMemberUseCase.updateMember(memberId, memberName, memberPhone);
  }

  @Transactional
  public void createDeliveryAddress(
      Long memberId, String zipCode, String address, String addressDetail) {
    orderCreateDeliveryAddress.createDeliveryAddress(memberId, zipCode, address, addressDetail);
  }
}
