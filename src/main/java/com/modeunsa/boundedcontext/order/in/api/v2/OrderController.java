package com.modeunsa.boundedcontext.order.in.api.v2;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
