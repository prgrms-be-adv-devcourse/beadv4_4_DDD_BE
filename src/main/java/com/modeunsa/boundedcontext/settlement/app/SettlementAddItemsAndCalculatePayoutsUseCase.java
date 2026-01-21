package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.domain.PayoutAmounts;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.config.SettlementConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementAddItemsAndCalculatePayoutsUseCase {
  private final SettlementRepository settlementRepository;
  private final SettlementConfig settlementConfig;

  public List<SettlementItem> addItemsAndCalculatePayouts(
      SettlementCandidateItem settlementCandidateItem) {
    Settlement sellerSettlement =
        getOrCreateSettlement(
            settlementCandidateItem.getSellerMemberId(),
            settlementCandidateItem.getPurchaseConfirmedAt(),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT);

    Long systemMemberId = settlementConfig.getSystemMemberId();

    Settlement feeSettlement =
        getOrCreateSettlement(
            systemMemberId,
            settlementCandidateItem.getPurchaseConfirmedAt(),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE);

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
            systemMemberId,
            payoutAmounts.feeAmount(),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
            settlementCandidateItem.getPurchaseConfirmedAt()));

    return items;
  }

  private Settlement getOrCreateSettlement(
      Long sellerMemberId, LocalDateTime purchaseConfirmedAt, SettlementEventType type) {
    int year = purchaseConfirmedAt.getYear();
    int month = purchaseConfirmedAt.getMonthValue();

    return settlementRepository
        .findBySellerMemberIdAndSettlementYearAndSettlementMonth(sellerMemberId, year, month)
        .orElseGet(
            () -> settlementRepository.save(Settlement.create(sellerMemberId, year, month, type)));
  }
}
