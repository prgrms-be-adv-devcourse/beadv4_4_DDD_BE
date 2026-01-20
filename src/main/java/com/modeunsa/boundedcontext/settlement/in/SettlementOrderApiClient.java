package com.modeunsa.boundedcontext.settlement.in;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SettlementOrderApiClient {

  // TODO: 실제 API 호출로 교체
  public List<SettlementOrderItemDto> getSettlementTargetOrders(int page, int size) {
    // 가데이터: 첫 페이지만 데이터 반환
    if (page > 0) {
      return List.of();
    }

    List<SettlementOrderItemDto> dummyOrders = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      dummyOrders.add(
          new SettlementOrderItemDto(
              (long) i, // orderItemId
              100L + i, // buyerMemberId
              1L, // sellerMemberId (판매자)
              new BigDecimal("10000"), // amount
              LocalDateTime.now().minusDays(1) // paymentAt
              ));
    }
    return dummyOrders;
  }
}
