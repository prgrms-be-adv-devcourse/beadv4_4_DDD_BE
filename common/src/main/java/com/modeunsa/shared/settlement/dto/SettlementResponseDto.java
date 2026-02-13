package com.modeunsa.shared.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementResponseDto {
  private Long id;
  @Builder.Default private BigDecimal totalSalesAmount = BigDecimal.ZERO;
  @Builder.Default private BigDecimal feeAmount = BigDecimal.ZERO;
  @Builder.Default private BigDecimal amount = BigDecimal.ZERO;
  private LocalDateTime payoutAt;
  @Builder.Default private List<SettlementItemResponseDto> items = new ArrayList<>();

  public static SettlementResponseDto create(
      Long id, BigDecimal feeAmount, BigDecimal amount, LocalDateTime payoutAt) {
    return SettlementResponseDto.builder()
        .id(id)
        .totalSalesAmount(feeAmount.add(amount))
        .feeAmount(feeAmount)
        .amount(amount)
        .payoutAt(payoutAt)
        .build();
  }

  public void addItem(SettlementItemResponseDto settlementItemResponseDto) {
    items.add(settlementItemResponseDto);
  }
}
