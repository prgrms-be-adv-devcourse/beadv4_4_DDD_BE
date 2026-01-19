package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
  Optional<Settlement> findBySellerMemberId(Long sellerMemberId);

  Optional<Settlement> findBySellerMemberIdAndSettlementYearAndSettlementMonth(
      Long sellerMemberId, int settlementYear, int settlementMonth);

  Page<Settlement> findByPayoutAtIsNullAndSettlementYearAndSettlementMonth(
      int settlementYear, int settlementMonth, Pageable pageable);
}
