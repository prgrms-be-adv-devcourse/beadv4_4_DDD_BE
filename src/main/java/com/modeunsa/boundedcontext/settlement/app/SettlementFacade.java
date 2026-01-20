package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.shared.settlement.dto.SettlementResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementFacade {
  private final SettlementAddItemsAndCalculatePayoutsUseCase settlementProcessOrderUseCase;
  private final SettlementSaveItemsUseCase settlementSaveItemsUseCase;
  private final SettlementSyncMemberUseCase settlementSyncMemberUseCase;
  private final SettlementCollectCandidateItemsUseCase settlementCollectCandidateItemsUseCase;
  private final SettlementSupport settlementSupport;

  @Transactional
  public SettlementMember syncMember(Long memberId, String memberRole) {
    return settlementSyncMemberUseCase.syncMember(memberId, memberRole);
  }

  @Transactional
  public List<SettlementItem> addItemsAndCalculatePayouts(
      SettlementOrderItemDto settlementOrderItemDto) {
    return settlementProcessOrderUseCase.addItemsAndCalculatePayouts(settlementOrderItemDto);
  }

  @Transactional
  public void saveItems(List<SettlementItem> items) {
    settlementSaveItemsUseCase.saveItems(items);
  }

  @Transactional
  public SettlementResponseDto getSettlement(Long memberId, int year, int month) {
    return settlementSupport.getSettlement(memberId, year, month);
  }

  @Transactional
  public void collectCandidateItems(Long orderId) {
    settlementCollectCandidateItemsUseCase.collectCandidateItems(orderId);
  }
}
