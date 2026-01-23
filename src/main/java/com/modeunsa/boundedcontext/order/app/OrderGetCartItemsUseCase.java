package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.OrderCartItemRepository;
import com.modeunsa.shared.order.dto.CartItemDto;
import com.modeunsa.shared.order.dto.CartItemsResponseDto;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductOrderValidateRequest;
import com.modeunsa.shared.product.out.ProductApiClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderGetCartItemsUseCase {

  private final OrderCartItemRepository orderCartItemRepository;
  private final OrderSupport orderSupport;
  private final OrderMapper orderMapper;
  private final ProductApiClient productApiClient;

  public CartItemsResponseDto getCartItems(long memberId) {
    // 회원 확인
    orderSupport.existsByMemberId(memberId);

    List<CartItem> cartItems = orderCartItemRepository.findAllByMemberId(memberId);

    // 장바구니 비어있을 경우 바로 반환
    if (cartItems.isEmpty()) {
      return new CartItemsResponseDto(memberId, 0, BigDecimal.ZERO, List.of());
    }

    // 상품 정보 조회
    Map<Long, ProductOrderResponse> productMap = getRealTimeProductMap(cartItems);

    // DTO 변환
    List<CartItemDto> itemDtos = createCartItemDtos(cartItems, productMap);

    return createCartItemsResponse(memberId, itemDtos);
  }

  // ================== Private Methods (세부 구현) ==================

  // [상품 정보 조회] : 장바구니 아이템에서 ID를 추출하여 상품 정보를 조회 후 Map으로 변환
  private Map<Long, ProductOrderResponse> getRealTimeProductMap(List<CartItem> cartItems) {
    List<Long> productIds = cartItems.stream().map(CartItem::getProductId).toList();

    // 상품 모듈 API 호출
    List<ProductOrderResponse> responses =
        productApiClient.validateOrderProducts(new ProductOrderValidateRequest(productIds));

    return responses.stream().collect(Collectors.toMap(ProductOrderResponse::productId, p -> p));
  }

  // 장바구니 상품 dto 변환 : 장바구니 아이템과 상품 정보 합침
  private List<CartItemDto> createCartItemDtos(
      List<CartItem> cartItems, Map<Long, ProductOrderResponse> productMap) {
    return cartItems.stream()
        .map(
            cartItem -> {
              ProductOrderResponse product = productMap.get(cartItem.getProductId());
              boolean isAvailable = isProductAvailable(product, cartItem.getQuantity());
              return orderMapper.toCartItemDto(cartItem, product, isAvailable);
            })
        .toList();
  }

  // 가용성 판단 메서드
  private boolean isProductAvailable(ProductOrderResponse product, int cartQuantity) {
    if (product == null) {
      return false; // 상품이 삭제됨
    }
    // 재고 체크 (TODO: product.status() == SALE 조건도 추가)
    return product.stock() >= cartQuantity;
  }

  // 최종 dto 변환 : 총 수량과 총 금액을 계산하여 포장
  private CartItemsResponseDto createCartItemsResponse(long memberId, List<CartItemDto> itemDtos) {
    int totalQuantity =
        itemDtos.stream().filter(CartItemDto::isAvailable).mapToInt(CartItemDto::quantity).sum();

    BigDecimal totalAmount =
        itemDtos.stream()
            .filter(CartItemDto::isAvailable)
            .map(dto -> dto.salePrice().multiply(BigDecimal.valueOf(dto.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new CartItemsResponseDto(memberId, totalQuantity, totalAmount, itemDtos);
  }
}
