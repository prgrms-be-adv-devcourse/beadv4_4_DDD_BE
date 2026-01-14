package com.modeunsa.boundedcontext.order.in;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
