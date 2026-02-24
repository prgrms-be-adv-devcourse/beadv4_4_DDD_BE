package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementCandidateItemRepository
    extends JpaRepository<SettlementCandidateItem, Long> {

  @Query(
      """
      SELECT s FROM SettlementCandidateItem s
      WHERE s.collectedAt IS NULL
        AND s.purchaseConfirmedAt >= :startInclusive
        AND s.purchaseConfirmedAt < :endExclusive
      ORDER BY s.id ASC
      """)
  List<SettlementCandidateItem> findUncollectedItems(
      @Param("startInclusive") LocalDateTime startInclusive,
      @Param("endExclusive") LocalDateTime endExclusive);
}
