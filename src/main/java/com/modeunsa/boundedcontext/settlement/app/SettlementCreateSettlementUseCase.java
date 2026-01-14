package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementCreateSettlementUseCase {
  private final SettlementRepository settlementRepository;

  public Settlement createSettlement(long sellerMemberId) {
    return settlementRepository.save(
        Settlement.builder().amount(0).sellerMemberId(sellerMemberId).build());
  }
}
