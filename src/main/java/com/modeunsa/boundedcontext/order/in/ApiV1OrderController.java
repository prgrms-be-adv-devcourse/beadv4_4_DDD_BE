package com.modeunsa.boundedcontext.order.in;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
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
import org.springframework.web.bind.annotation.GetMapping;
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
      // @AuthenticationPrincipal Long memberId // 나중에 시큐리티 적용 시
      @RequestBody @Valid CreateCartItemRequestDto requestDto) {
    // [TODO] 실제 로그인한 유저 ID를 가져오는 로직 추가
    long memberId = 1;

    CreateCartItemResponseDto dto = orderFacade.createCartItem(memberId, requestDto);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "단건 주문 생성 기능", description = "단건 상품을 주문하는 기능입니다.")
  @PostMapping
  public ResponseEntity<ApiResponse> createOrder(
      // @AuthenticationPrincipal Long memberId // 나중에 시큐리티 적용 시
      @RequestBody @Valid CreateOrderRequestDto requestDto) {
    // [TODO] 실제 로그인한 유저 ID를 가져오는 로직 추가
    long memberId = 1;

    OrderResponseDto dto = orderFacade.createOrder(memberId, requestDto);

    return ApiResponse.onSuccess(SuccessStatus.OK, dto);
  }

  @Operation(summary = "주문 목록 조회 기능", description = "생성한 주문들의 목록을 확인할 수 있는 기능입니다.")
  @GetMapping
  public ResponseEntity<ApiResponse> getOrders(
      // @AuthenticationPrincipal Long memberId // 나중에 시큐리티 적용 시
      @ParameterObject
          @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    // [TODO] 실제 로그인한 유저 ID를 가져오는 로직 추가
    long memberId = 1;

    Page<OrderListResponseDto> pageDto = orderFacade.getOrders(memberId, pageable);

    return ApiResponse.onSuccess(SuccessStatus.OK, pageDto);
  }
}
