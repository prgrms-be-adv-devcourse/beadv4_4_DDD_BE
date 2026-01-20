package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementCollectSettlementItemsUseCase {
  private final SettlementRepository settlementRepository;

  public void collectSettlementItems() {
    log.info("[UseCase] collectSettlementItems 실행");
    // TODO:
    // get order items api 추가 이후 추가 예정
    // List<OrderItemDto> -> List<SettlementItem> 추후 추가 예정
  }
}
