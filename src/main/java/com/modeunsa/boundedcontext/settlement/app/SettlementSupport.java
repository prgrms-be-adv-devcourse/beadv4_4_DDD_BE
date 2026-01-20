package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.domain.policy.SettlementPolicy;
import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.settlement.dto.SettlementItemResponseDto;
import com.modeunsa.shared.settlement.dto.SettlementResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementSupport {

  private final SettlementRepository settlementRepository;
  private final SettlementMemberRepository settlementMemberRepository;
  private final SettlementCandidateItemRepository settlementCandidateItemRepository;

  public SettlementResponseDto getSettlement(Long sellerMemberId, int year, int month) {
    // 1. 정산서를 불러온다.
    Settlement settlement =
        settlementRepository
            .findBySellerMemberIdAndSettlementYearAndSettlementMonth(sellerMemberId, year, month)
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_NOT_FOUND));

    SettlementMember systemMember =
        settlementMemberRepository
            .findByName(SettlementPolicy.SYSTEM)
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_MEMBER_NOT_FOUND));

    Settlement feeSettlement =
        settlementRepository
            .findBySellerMemberIdAndSettlementYearAndSettlementMonth(
                systemMember.getId(), year, month)
            .orElseThrow(() -> new GeneralException(ErrorStatus.SETTLEMENT_NOT_FOUND));

    // 수수료를 map으로 저장
    Map<Long, BigDecimal> feeMap =
        feeSettlement.getItems().stream()
            .collect(Collectors.toMap(SettlementItem::getOrderItemId, SettlementItem::getAmount));

    // 2. 정산금, 수수료 두개를 불러와서 하나의 dto에 합산
    SettlementResponseDto settlementResponseDto =
        SettlementResponseDto.create(
            settlement.getId(),
            feeSettlement.getAmount(),
            settlement.getAmount(),
            settlement.getPayoutAt());

    settlement
        .getItems()
        .forEach(
            item -> {
              BigDecimal feeAmount = feeMap.getOrDefault(item.getOrderItemId(), BigDecimal.ZERO);
              BigDecimal totalSales = item.getAmount().add(feeAmount);

              settlementResponseDto.addItem(
                  SettlementItemResponseDto.builder()
                      .id(item.getId())
                      .orderItemId(item.getOrderItemId())
                      .sellerMemberId(item.getSellerMemberId())
                      .amount(item.getAmount())
                      .feeAmount(feeAmount)
                      .totalSalesAmount(totalSales)
                      .paymentAt(item.getPaymentAt())
                      .build());
            });

    return settlementResponseDto;
  }

  public Page<SettlementCandidateItem> getSettlementCandidateItems(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return settlementCandidateItemRepository
        .findByCollectedAtIsNullAndPaymentAtGreaterThanEqualAndPaymentAtLessThanOrderByIdAsc(
            startDate, endDate, pageable);
  }
}
