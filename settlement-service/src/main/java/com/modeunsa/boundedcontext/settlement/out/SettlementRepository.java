package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
  Optional<Settlement> findBySellerMemberId(Long sellerMemberId);

  Optional<Settlement> findBySellerMemberIdAndSettlementYearAndSettlementMonth(
      Long sellerMemberId, int settlementYear, int settlementMonth);

  Optional<Settlement> findBySellerMemberIdAndSettlementYearAndSettlementMonthAndType(
      Long sellerMemberId, int settlementYear, int settlementMonth, SettlementEventType type);

  List<Settlement> findByPayoutAtIsNullAndSettlementYearAndSettlementMonthOrderByIdAsc(
      int settlementYear, int settlementMonth);
}
