package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderCartItemRepository;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderProductRepository;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderSupport {
  private final OrderMemberRepository orderMemberRepository;
  private final OrderProductRepository orderProductRepository;
  private final OrderRepository orderRepository;
  private final OrderCartItemRepository orderCartItemRepository;

  // --- orderMember ---
  public long countMember() {
    return orderMemberRepository.count();
  }

  public OrderMember findByMemberId(Long memberId) {
    return orderMemberRepository
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_MEMBER_NOT_FOUND));
  }

  public void existsByMemberId(Long memberId) {
    if (!orderMemberRepository.existsById(memberId)) {
      throw new GeneralException(ErrorStatus.ORDER_MEMBER_NOT_FOUND);
    }
  }

  // --- orderProduct ---
  public long countProduct() {
    return orderProductRepository.count();
  }

  public OrderProduct findByProductId(Long productId) {
    return orderProductRepository
        .findById(productId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_PRODUCT_NOT_FOUND));
  }

  public void saveProduct(OrderProduct product) {
    orderProductRepository.save(product);
  }

  // 장바구니 비우기
  public void clearCart(Long memberId) {
    orderCartItemRepository.deleteByMemberId(memberId);
  }

  // --- order ---
  public long countOrder() {
    return orderRepository.count();
  }

  public Order findByOrderId(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));
  }

  public Order findTopByOrderMemberIdByOrderByIdDesc(Long memberId) {
    return orderRepository
        .findTopByOrderMemberIdOrderByIdDesc(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));
  }

  // --- cartItem ---
  public List<CartItem> getCartItemsByMemberId(Long memberId) {
    List<CartItem> cartItems = orderCartItemRepository.findAllByMemberId(memberId);
    if (cartItems.isEmpty()) {
      throw new GeneralException(ErrorStatus.ORDER_CART_EMPTY);
    }

    return cartItems;
  }

  public long countCartItem() {
    return orderCartItemRepository.count();
  }

  public List<Long> getRecentCartItems(Long memberId) {
    return orderCartItemRepository.findTop10ProductIdsByMemberIdOrderByCreatedAtDesc(memberId);
  }
}
