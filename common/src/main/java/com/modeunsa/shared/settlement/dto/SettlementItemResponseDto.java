package com.modeunsa.shared.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementItemResponseDto {
  private Long id;
  private Long orderItemId;
  private Long sellerMemberId;
  @Builder.Default private BigDecimal totalSalesAmount = BigDecimal.ZERO;
  @Builder.Default private BigDecimal feeAmount = BigDecimal.ZERO;
  @Builder.Default private BigDecimal amount = BigDecimal.ZERO;
  private LocalDateTime purchaseConfirmedAt;
}
