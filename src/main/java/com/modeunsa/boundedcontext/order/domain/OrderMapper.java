package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.shared.order.dto.CartItemDto;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderItemDto;
import com.modeunsa.shared.order.dto.OrderItemResponseDto;
import com.modeunsa.shared.order.dto.OrderListResponseDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  // 장바구니 상품
  // TODO: SecurityContext에서 memberId 추출해서 Auditing으로 createdBy필드 채우기
  @Mapping(target = "isAvailable", ignore = true)
  CartItem toCartItemEntity(long memberId, CreateCartItemRequestDto createCartItemRequestDto);

  CreateCartItemResponseDto toCreateCartItemResponseDto(CartItem cartItem);

  @Mapping(target = "salePrice", source = "product.salePrice") // ★ 상품의 가격(단가)을 DTO에 넣음
  @Mapping(target = "quantity", source = "cartItem.quantity")
  CartItemDto toCartItemDto(CartItem cartItem, OrderProduct product);

  // 주문 상품
  @Mapping(target = "productId", source = "product.productId")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "order", ignore = true)
  @Mapping(target = "sellerId", source = "product.sellerId")
  OrderItem toOrderItemEntity(
      ProductOrderResponse product, @Valid CreateOrderRequestDto requestDto);

  OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);

  @Mapping(target = "productId", source = "productId")
  OrderItemDto toItemDto(OrderItem orderItem);

  @Mapping(target = "productName", source = "productInfo.name")
  OrderItem toOrderItem(ProductOrderResponse productInfo, int quantity);

  // ---- 주문 ----
  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "memberId", source = "order.orderMember.id")
  OrderResponseDto toOrderResponseDto(Order order);

  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "orderItems", source = "orderItems")
  @Mapping(target = "memberId", source = "order.orderMember.id")
  OrderDto toOrderDto(Order order);

  // 단건 변환 메서드
  @Mapping(target = "orderId", source = "id")
  @Mapping(
      target = "repProductName",
      expression = "java(makeRepProductName(order.getOrderItems()))")
  @Mapping(target = "totalAmount", source = "totalAmount")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "orderedAt", source = "createdAt")
  OrderListResponseDto toOrderListResponseDto(Order order);

  // --- 메서드 ---

  // 대표 상품명 생성
  default String makeRepProductName(List<OrderItem> items) {
    if (items == null || items.isEmpty()) {
      return "상품 정보 없음";
    }

    String firstItemName = items.get(0).getProductName();
    int size = items.size();

    return (size == 1) ? firstItemName : firstItemName + " 외 " + (size - 1) + "건";
  }
}
