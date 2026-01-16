package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.shared.order.dto.CreateCartOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
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

  public OrderResponseDto createCartOrder(long memberId, CreateCartOrderRequestDto requestDto) {
    // 회원 및 장바구니 목록 조회
    OrderMember member = orderSupport.findByMemberId(memberId);
    List<CartItem> cartItems = orderSupport.getCartItemsByMemberId(memberId);
    List<OrderProduct> products = orderSupport.getProductsByCartItems(cartItems);

    // 장바구니에 담긴 상품들 각각 재고 차감 TODO : 동기처리
    decreaseProductStocks(cartItems, products);

    List<OrderItem> orderItems = createOrderItems(cartItems, products);

    // 주문 생성
    Order order =
        Order.createOrder(
            member,
            orderItems,
            requestDto.getReceiverName(),
            requestDto.getReceiverPhone(),
            requestDto.getZipcode(),
            requestDto.getAddressDetail());

    // 주문 저장
    orderRepository.save(order);

    // 장바구니 비우기
    orderSupport.clearCart(memberId);

    return orderMapper.toOrderResponseDto(order);
  }

  // 재고 차감 메서드
  private void decreaseProductStocks(List<CartItem> cartItems, List<OrderProduct> products) {
    Map<Long, OrderProduct> productMap =
        products.stream().collect(Collectors.toMap(OrderProduct::getId, p -> p));

    for (CartItem item : cartItems) {
      OrderProduct product = productMap.get(item.getProductId());
      // Product 엔티티의 decreaseStock 호출 (여기서 재고 부족시 에러 터짐)
      product.decreaseStock(item.getQuantity());
    }
  }

  // 장바구니 상품 -> 주문 상품
  private List<OrderItem> createOrderItems(List<CartItem> cartItems, List<OrderProduct> products) {
    Map<Long, OrderProduct> productMap =
        products.stream().collect(Collectors.toMap(OrderProduct::getId, p -> p));

    List<OrderItem> orderItems = new ArrayList<>();

    for (CartItem cartItem : cartItems) {
      OrderProduct realProduct = productMap.get(cartItem.getProductId());

      // OrderItem 생성
      OrderItem orderItem = orderMapper.toOrderItemFromCart(realProduct, cartItem);

      orderItems.add(orderItem);
    }
    return orderItems;
  }
}
