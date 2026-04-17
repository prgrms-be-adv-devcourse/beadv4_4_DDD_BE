package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementStatus;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
  Optional<Settlement> findBySellerMemberId(Long sellerMemberId);

  Optional<Settlement> findBySellerMemberIdAndSettlementYearAndSettlementMonth(
      Long sellerMemberId, int settlementYear, int settlementMonth);

  Optional<Settlement> findBySellerMemberIdAndSettlementYearAndSettlementMonthAndType(
      Long sellerMemberId, int settlementYear, int settlementMonth, SettlementEventType type);

  List<Settlement> findByPayoutAtIsNullAndSettlementYearAndSettlementMonthOrderByIdAsc(
      int settlementYear, int settlementMonth);

  List<Settlement> findBySettlementYearAndSettlementMonthAndStatusOrderByIdAsc(
      int settlementYear, int settlementMonth, SettlementStatus status);

  List<Settlement> findByBatchExecutionIdAndStatusOrderByIdAsc(
      Long batchExecutionId, SettlementStatus status);

  // reserveStep용: PENDING → PROCESSING 일괄 전환
  @Modifying
  @Transactional
  @Query(
      """
      UPDATE Settlement s
      SET s.status = 'PROCESSING', s.batchExecutionId = :batchExecutionId, s.processingAt = :now
      WHERE s.settlementYear = :year AND s.settlementMonth = :month
      AND s.status = 'PENDING'
      """)
  int bulkMarkProcessing(
      @Param("batchExecutionId") Long batchExecutionId,
      @Param("year") int year,
      @Param("month") int month,
      @Param("now") LocalDateTime now);

  // completeStep용: PROCESSING → COMPLETED 일괄 전환
  @Modifying
  @Transactional
  @Query(
      """
      UPDATE Settlement s
      SET s.status = 'COMPLETED', s.payoutAt = :now
      WHERE s.batchExecutionId = :batchExecutionId AND s.status = 'PROCESSING'
      """)
  int bulkCompletePayout(
      @Param("batchExecutionId") Long batchExecutionId, @Param("now") LocalDateTime now);

  // Listener용: PROCESSING → PENDING 일괄 롤백
  @Modifying
  @Transactional
  @Query(
      """
      UPDATE Settlement s
      SET s.status = 'PENDING', s.batchExecutionId = NULL, s.processingAt = NULL
      WHERE s.batchExecutionId = :batchExecutionId AND s.status = 'PROCESSING'
      """)
  int bulkRollbackToPending(@Param("batchExecutionId") Long batchExecutionId);
}
