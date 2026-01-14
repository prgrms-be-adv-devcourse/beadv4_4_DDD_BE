package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.CreateOrderResponseDto;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderItemDto;
import com.modeunsa.shared.order.dto.OrderItemResponseDto;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  // 장바구니 상품
  // TODO: SecurityContext에서 memberId 추출해서 Auditing으로 createdBy필드 채우기
  @Mapping(target = "isAvailable", ignore = true)
  CartItem toCartItemEntity(long memberId, CreateCartItemRequestDto createCartItemRequestDto);

  CreateCartItemResponseDto toCreateCartItemResponseDto(CartItem cartItem);

  // 주문 상품
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "order", ignore = true)
  OrderItem toOrderItemEntity(OrderProduct product, @Valid CreateOrderRequestDto requestDto);

  OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);

  @Mapping(target = "productId", source = "productId")
  OrderItemDto toItemDto(OrderItem orderItem);

  // 주문
  @Mapping(target = "orderMember", source = "member")
  @Mapping(target = "totalAmount", source = "salePrice")
  @Mapping(target = "status", constant = "PENDING_PAYMENT")
  @Mapping(target = "orderNo", expression = "java(generateOrderNo(member.getId()))")
  @Mapping(target = "paymentDeadlineAt", expression = "java(calculateDeadline())")
  @Mapping(target = "zipcode", source = "requestDto.zipcode")
  @Mapping(target = "addressDetail", source = "requestDto.addressDetail")
  @Mapping(target = "orderItems", ignore = true)
  Order toOrderEntity(
      OrderMember member, BigDecimal salePrice, @Valid CreateOrderRequestDto requestDto);

  @Mapping(target = "orderId", source = "id")
  @Mapping(target = "memberId", source = "order.orderMember.id")
  CreateOrderResponseDto toOrderCreateResponseDto(Order order);

  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "orderItems", source = "orderItems")
  @Mapping(target = "memberId", source = "order.orderMember.id")
  OrderDto toOrderDto(Order order);

  // --- 메서드 ---
  // 주문 생성시 결제 마감기한 설정
  default LocalDateTime calculateDeadline() {
    return LocalDateTime.now().plusMinutes(30);
  }

  // 주문번호 생성
  default String generateOrderNo(Long memberId) {
    // 날짜와 시간-유저 ID(yyyyMMddHHmmssSSS-%04d 포맷팅)
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        + "-"
        + String.format("%04d", memberId % 10000);
  }
}
