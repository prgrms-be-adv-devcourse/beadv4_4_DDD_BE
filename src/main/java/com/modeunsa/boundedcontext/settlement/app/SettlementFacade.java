package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementFacade {
  // TODO: 판매자 가입시에 정산서 생성 이벤트
  private final SettlementCreateSettlementUseCase settlementCreateSettlementUseCase;
  private final SettlementCollectSettlementItemsUseCase settlementAddSettlementItemsUseCase;
  private final SettlementCalculatePayoutsUseCase settlementCalculatePayoutsUseCase;

  @Transactional
  public Settlement createSettlement(long sellerMemberId) {
    return settlementCreateSettlementUseCase.createSettlement(sellerMemberId);
  }

  @Transactional
  public void collectSettlementItems() {
    settlementAddSettlementItemsUseCase.collectSettlementItems();
  }

  @Transactional
  public void calculatePayouts() {
    settlementCalculatePayoutsUseCase.calculatePayouts();
  }
}
