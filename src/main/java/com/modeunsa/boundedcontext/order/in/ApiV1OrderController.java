package com.modeunsa.boundedcontext.order.in;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.order.dto.CartItemsResponseDto;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import com.modeunsa.shared.order.dto.CreateCartOrderRequestDto;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderListResponseDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order", description = "주문 도메인 API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ApiV1OrderController {

  private final OrderFacade orderFacade;

  @Operation(summary = "장바구니에 상품 추가 기능", description = "장바구니에 상품을 추가하는 기능입니다.")
  @PostMapping("/cart/item")
  public ResponseEntity<ApiResponse> createCartItem(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody @Valid CreateCartItemRequestDto requestDto) {

    Long memberId = user.getMemberId();
    CreateCartItemResponseDto dto = orderFacade.createCartItem(memberId, requestDto);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "단건 주문 생성 기능", description = "단건 상품을 주문하는 기능입니다.")
  @PostMapping
  public ResponseEntity<ApiResponse> createOrder(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody @Valid CreateOrderRequestDto requestDto) {

    Long memberId = user.getMemberId();
    OrderResponseDto dto = orderFacade.createOrder(memberId, requestDto);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "장바구니 주문 생성 기능", description = "장바구니에 있는 모든 상품을 주문하는 기능입니다.")
  @PostMapping("/cart-order")
  public ResponseEntity<ApiResponse> createCartOrder(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody @Valid CreateCartOrderRequestDto requestDto) {

    Long memberId = user.getMemberId();
    OrderResponseDto dto = orderFacade.createCartOrder(memberId, requestDto);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "주문 목록 조회 기능", description = "생성한 주문들의 목록을 확인할 수 있는 기능입니다.")
  @GetMapping
  public ResponseEntity<ApiResponse> getOrders(
      @AuthenticationPrincipal CustomUserDetails user,
      @ParameterObject
          @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    Long memberId = user.getMemberId();
    Page<OrderListResponseDto> pageDto = orderFacade.getOrders(memberId, pageable);

    return ApiResponse.onSuccess(SuccessStatus.OK, pageDto);
  }

  @Operation(summary = "회원 주문 취소 요청 기능", description = "회원이 배송 전 상태인 주문을 취소 요청할 수 있는 기능입니다.")
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<ApiResponse> cancelOrder(
      @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long orderId) {

    Long memberId = user.getMemberId();
    OrderResponseDto dto = orderFacade.cancelOrder(memberId, orderId);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "회원 주문 환불 요청 기능", description = "배송 완료된 주문을 환불 요청할 수 있는 기능입니다.")
  @PostMapping("/{orderId}/refund")
  public ResponseEntity<ApiResponse> refundOrder(
      @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long orderId) {

    Long memberId = user.getMemberId();
    OrderResponseDto dto = orderFacade.refundOrder(memberId, orderId);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "장바구니 상품 목록 조회 기능", description = "장바구니 상품 목록을 확인할 수 있는 기능입니다.")
  @GetMapping("/cart-items")
  public ResponseEntity<ApiResponse> getCartItem(@AuthenticationPrincipal CustomUserDetails user) {

    Long memberId = user.getMemberId();
    CartItemsResponseDto dto = orderFacade.getCartItems(memberId);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "내부 주문 조회 기능", description = "정산 모듈에서 사용하는 주문 조회 API입니다.")
  @GetMapping("/internal/{id}")
  public OrderDto getInternalOrder(@PathVariable Long id) {
    return orderFacade.getInternalOrder(id);
  }

  @Operation(summary = "주문 조회 기능", description = "클라이언트에서 결제 요청을 하기 위한 주문 상세 조회 API입니다.")
  @GetMapping("/{orderId}")
  public OrderDto getOrder(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long orderId) {
    Long memberId = user.getMemberId();
    return orderFacade.getOrder(memberId, orderId);
  }
}
