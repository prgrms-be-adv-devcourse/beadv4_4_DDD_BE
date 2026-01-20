package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementCandidateItemRepository
    extends JpaRepository<SettlementCandidateItem, Long> {

  Page<SettlementCandidateItem>
      findByCollectedAtIsNullAndPaymentAtGreaterThanEqualAndPaymentAtLessThanOrderByIdAsc(
          LocalDateTime startInclusive, LocalDateTime endExclusive, Pageable pageable);
}
