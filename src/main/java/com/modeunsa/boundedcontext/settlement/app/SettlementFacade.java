package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementAddItemsAndCalculatePayoutsUseCase;
import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementCreateSettlementUseCase;
import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementSaveItemsUseCase;
import com.modeunsa.boundedcontext.settlement.app.usecase.SettlementSyncMemberUseCase;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementFacade {
  private final SettlementCreateSettlementUseCase settlementCreateSettlementUseCase;
  private final SettlementAddItemsAndCalculatePayoutsUseCase settlementProcessOrderUseCase;
  private final SettlementSaveItemsUseCase settlementSaveItemsUseCase;
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
  public List<SettlementItem> addItemsAndCalculatePayouts(SettlementOrderItemDto order) {
    return settlementProcessOrderUseCase.addItemsAndCalculatePayouts(order);
  }

  @Transactional
  public void saveItems(List<SettlementItem> items) {
    settlementSaveItemsUseCase.saveItems(items);
  }
}
