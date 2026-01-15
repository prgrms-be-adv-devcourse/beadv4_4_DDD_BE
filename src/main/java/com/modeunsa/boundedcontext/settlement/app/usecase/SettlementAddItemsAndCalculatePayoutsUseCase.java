package com.modeunsa.boundedcontext.settlement.app.usecase;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementOrderItemDto;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementAddItemsAndCalculatePayoutsUseCase {
  private static final BigDecimal FEE_RATE = BigDecimal.valueOf(0.1);

  private final SettlementRepository settlementRepository;
  private final SettlementMemberRepository settlementMemberRepository;

  public List<SettlementItem> addItemsAndCalculatePayouts(SettlementOrderItemDto order) {
    Settlement sellerSettlement =
        settlementRepository
            .findBySellerMemberId(order.sellerMemberId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_NOT_FOUND));

    SettlementMember systemMember =
        settlementMemberRepository
            .findByName("SYSTEM")
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_MEMBER_NOT_FOUND));

    Settlement feeSettlement =
        settlementRepository
            .findBySellerMemberId(systemMember.getId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_NOT_FOUND));

    BigDecimal feeAmount = order.amount().multiply(FEE_RATE).setScale(0, RoundingMode.HALF_UP);
    BigDecimal sellerAmount = order.amount().subtract(feeAmount);

    List<SettlementItem> items = new ArrayList<>();

    items.add(
        sellerSettlement.addItem(
            order.orderItemId(),
            order.buyerMemberId(),
            order.sellerMemberId(),
            sellerAmount,
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
            order.paymentAt()));

    items.add(
        feeSettlement.addItem(
            order.orderItemId(),
            order.buyerMemberId(),
            systemMember.getId(),
            feeAmount,
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
            order.paymentAt()));

    return items;
  }
}
