package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import com.modeunsa.boundedcontext.settlement.domain.PayoutAmounts;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
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

  public List<SettlementItem> addItemsAndCalculatePayouts(SettlementOrderItemDto order) {
    Settlement sellerSettlement = getOrCreateSettlement(order.sellerMemberId(), order.paymentAt());

    SettlementMember systemMember =
        settlementMemberRepository
            .findByName(SettlementPolicy.SYSTEM)
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_MEMBER_NOT_FOUND));

    Settlement feeSettlement = getOrCreateSettlement(systemMember.getId(), order.paymentAt());

    PayoutAmounts payoutAmounts = Settlement.calculatePayouts(order.amount());

    List<SettlementItem> items = new ArrayList<>(2);

    items.add(
        sellerSettlement.addItem(
            order.orderItemId(),
            order.buyerMemberId(),
            order.sellerMemberId(),
            payoutAmounts.sellerAmount(),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
            order.paymentAt()));

    items.add(
        feeSettlement.addItem(
            order.orderItemId(),
            order.buyerMemberId(),
            systemMember.getId(),
            payoutAmounts.feeAmount(),
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
            order.paymentAt()));

    return items;
  }

  private Settlement getOrCreateSettlement(Long sellerMemberId, LocalDateTime paymentAt) {
    int year = paymentAt.getYear();
    int month = paymentAt.getMonthValue();

    return settlementRepository
        .findBySellerMemberIdAndSettlementYearAndSettlementMonth(sellerMemberId, year, month)
        .orElseGet(() -> settlementRepository.save(Settlement.create(sellerMemberId, year, month)));
  }
}
