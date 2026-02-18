package com.modeunsa.boundedcontext.order.in.api.v2;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.order.dto.DeleteCartItemsRequestDto;
import com.modeunsa.shared.order.dto.OrderDeliveryRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order", description = "주문 도메인 API")
@RestController("orderV2Controller")
@RequestMapping("/api/v2/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderFacade orderFacade;

  @Operation(summary = "내부 장바구니 상품 조회", description = "상품 추천 기능을 위한 최근 장바구니 아이템 10개를 조회하는 api입니다.")
  @GetMapping("/internal/cart-items/{memberId}")
  public List<Long> getRecentCartItems(
      @PathVariable Long memberId, @RequestParam(defaultValue = "10") @Positive int cartItemSize) {
    return orderFacade.getRecentCartItems(memberId, cartItemSize);
  }

  @Operation(summary = "장바구니 아이템 선택 삭제 기능", description = "장바구니에 있는 아이템 중 선택된 아이템들만 삭제되는 기능입니다.")
  @DeleteMapping("/cart-items")
  public ResponseEntity<ApiResponse> deleteCartItems(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody @Valid DeleteCartItemsRequestDto request) {
    Long memberId = user.getMemberId();
    orderFacade.deleteCartItems(memberId, request);
    return ApiResponse.onSuccess(SuccessStatus.NO_CONTENT);
  }

  @Operation(summary = "장바구니 아이템 전체 삭제 기능", description = "장바구니에 있는 모든 아이템을 삭제되는 기능입니다.")
  @DeleteMapping("/cart-items/all")
  public ResponseEntity<ApiResponse> deleteAllCartItems(
      @AuthenticationPrincipal CustomUserDetails user) {
    Long memberId = user.getMemberId();
    orderFacade.deleteAllCartItems(memberId);
    return ApiResponse.onSuccess(SuccessStatus.NO_CONTENT);
  }

  @Operation(summary = "배송정보 입력 기능", description = "주문배송정보를 입력하는 기능입니다.")
  @PatchMapping("/delivery-info")
  public ResponseEntity<ApiResponse> addOrderDeliveryInfo(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long orderId,
      @RequestBody @Valid OrderDeliveryRequestDto orderDeliveryRequestDto) {
    Long memberId = user.getMemberId();
    orderFacade.addOrderDeliveryInfo(memberId, orderId, orderDeliveryRequestDto);
    return ApiResponse.onSuccess(SuccessStatus.NO_CONTENT);
  }

  @Operation(summary = "내부 결제대기 주문 개수 조회 기능 ", description = "결제 대기중인 주문 개수를 조회하는 내부 모듈 api입니다.")
  @GetMapping("/internal/pending-count/{productId}")
  public int getPendingCount(@PathVariable Long productId) {
    return orderFacade.getPendingCount(productId);
  }
}
