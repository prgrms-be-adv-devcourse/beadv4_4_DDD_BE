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
import java.util.stream.Collectors;
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

  // 상품 목록 조회
  public List<OrderProduct> findAllOrderProductByProductId(List<Long> productIds) {
    return orderProductRepository.findAllById(productIds);
  }

  // 장바구니에 담겨있는 실제 상품 목록 조회(장바구니에 담긴 상품 ID들을 뽑아서 실제 상품 정보 가져옴)
  public List<OrderProduct> getProductsByCartItems(List<CartItem> cartItems) {
    List<Long> productIds =
        cartItems.stream().map(CartItem::getProductId).collect(Collectors.toList());

    List<OrderProduct> products = findAllOrderProductByProductId(productIds);

    // 장바구니엔 있는데 상품이 삭제된 경우 검증
    if (products.size() != productIds.size()) {
      throw new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND);
    }

    return products;
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
}
