package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementCalculatePayoutsUseCase;
import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementCollectSettlementItemsUseCase;
import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementCreateSettlementUseCase;
import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementSyncMemberUseCase;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.shared.settlement.dto.SettlementMemberDto;
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
  private final SettlementSyncMemberUseCase settlementSyncMemberUseCase;

  @Transactional
  public SettlementMember syncMember(Long memberId, String memberRole) {
    return settlementSyncMemberUseCase.syncMember(memberId, memberRole);
  }

  @Transactional
  public Settlement createSettlement(Long sellerMemberId) {
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
