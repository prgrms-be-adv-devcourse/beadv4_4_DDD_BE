package com.modeunsa.boundedcontext.settlement.app;

import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementClaimCandidateItemUseCase {
  private final SettlementCandidateItemRepository settlementCandidateItemRepository;

  @Transactional
  public boolean claim(Long candidateItemId) {
    return settlementCandidateItemRepository.markCollectedIfUncollected(
            candidateItemId, LocalDateTime.now())
        == 1;
  }
}
