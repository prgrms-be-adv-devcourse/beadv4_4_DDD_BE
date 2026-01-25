package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.config.SettlementConfig;
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
  private final SettlementCandidateItemRepository settlementCandidateItemRepository;
  private final SettlementConfig settlementConfig;

  public SettlementResponseDto getSettlement(Long sellerMemberId, int year, int month) {
    // 1. 정산서를 불러온다. (여러 개가 있을 수 있으므로 첫 번째 것을 사용)
    java.util.List<Settlement> sellerSettlements =
        settlementRepository.findAllBySellerMemberIdAndSettlementYearAndSettlementMonth(
            sellerMemberId, year, month);

    if (sellerSettlements.isEmpty()) {
      throw new GeneralException(ErrorStatus.SETTLEMENT_NOT_FOUND);
    }

    Settlement settlement = sellerSettlements.get(0);

    Long systemMemberId = settlementConfig.getSystemMemberId();

    java.util.List<Settlement> feeSettlements =
        settlementRepository.findAllBySellerMemberIdAndSettlementYearAndSettlementMonth(
            systemMemberId, year, month);

    if (feeSettlements.isEmpty()) {
      throw new GeneralException(ErrorStatus.SETTLEMENT_NOT_FOUND);
    }

    Settlement feeSettlement = feeSettlements.get(0);

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
                      .purchaseConfirmedAt(item.getPurchaseConfirmedAt())
                      .build());
            });

    return settlementResponseDto;
  }

  public Page<SettlementCandidateItem> getSettlementCandidateItems(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return settlementCandidateItemRepository.findUncollectedItems(startDate, endDate, pageable);
  }
}
