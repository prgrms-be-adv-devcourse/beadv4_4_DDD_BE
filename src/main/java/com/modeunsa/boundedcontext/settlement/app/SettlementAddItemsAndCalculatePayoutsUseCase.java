package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.domain.PayoutAmounts;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.domain.policy.SettlementPolicy;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementAddItemsAndCalculatePayoutsUseCase {
  private final SettlementRepository settlementRepository;
  private final SettlementMemberRepository settlementMemberRepository;

  public List<SettlementItem> addItemsAndCalculatePayouts(
      SettlementCandidateItem settlementCandidateItem) {
    Settlement sellerSettlement =
        getOrCreateSettlement(
            settlementCandidateItem.getSellerMemberId(),
            settlementCandidateItem.getPurchaseConfirmedAt());

    SettlementMember systemMember =
        settlementMemberRepository
            .findByName(SettlementPolicy.SYSTEM)
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_MEMBER_NOT_FOUND));

    Settlement feeSettlement =
        getOrCreateSettlement(
            systemMember.getId(), settlementCandidateItem.getPurchaseConfirmedAt());

    PayoutAmounts payoutAmounts = Settlement.calculatePayouts(settlementCandidateItem.getAmount());

    List<SettlementItem> items = new ArrayList<>(2);

    items.add(
        sellerSettlement.addItem(
            settlementCandidateItem.getOrderItemId(),
            settlementCandidateItem.getBuyerMemberId(),
            settlementCandidateItem.getSellerMemberId(),
            payoutAmounts.sellerAmount(),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
            settlementCandidateItem.getPurchaseConfirmedAt()));

    items.add(
        feeSettlement.addItem(
            settlementCandidateItem.getOrderItemId(),
            settlementCandidateItem.getBuyerMemberId(),
            systemMember.getId(),
            payoutAmounts.feeAmount(),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
            settlementCandidateItem.getPurchaseConfirmedAt()));

    return items;
  }

  private Settlement getOrCreateSettlement(Long sellerMemberId, LocalDateTime purchaseConfirmedAt) {
    int year = purchaseConfirmedAt.getYear();
    int month = purchaseConfirmedAt.getMonthValue();

    return settlementRepository
        .findBySellerMemberIdAndSettlementYearAndSettlementMonth(sellerMemberId, year, month)
        .orElseGet(() -> settlementRepository.save(Settlement.create(sellerMemberId, year, month)));
  }
}
