package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, Long> {

  // clearAutomatically : delete 전에 영속성 컨텍스트 flush
  // flushAutomatically : delete 후 영속성 컨텍스트 clear
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM PaymentOutboxEvent po " + "WHERE po.id IN :ids")
  int deleteAlreadySentEventBefore(@Param("ids") List<Long> ids);

  @Modifying
  @Query("UPDATE PaymentOutboxEvent e SET e.status = :status, e.updatedAt = :now WHERE e.id = :id")
  void updateStatus(
      @Param("id") Long id,
      @Param("status") OutboxStatus paymentOutboxStatus,
      @Param("now") LocalDateTime now);

  @Modifying
  @Query(
      "UPDATE PaymentOutboxEvent e SET e.status = :status, e.sentAt = :sentAt, "
          + "e.updatedAt = :sentAt WHERE e.id = :id")
  int markSent(
      @Param("id") Long id,
      @Param("status") OutboxStatus status,
      @Param("sentAt") LocalDateTime sentAt);

  @Modifying(clearAutomatically = true)
  @Query(
      """
        UPDATE PaymentOutboxEvent e SET
          e.lastErrorMessage = :errorMessage,
          e.updatedAt = :now,
          e.retryCount = e.retryCount + 1,
          e.status = CASE WHEN (e.retryCount + 1) >= :maxRetry
                         THEN :failedStatus
                         ELSE :pendingStatus END
        WHERE e.id = :id
      """)
  void markFailed(
      @Param("id") Long id,
      @Param("errorMessage") String errorMessage,
      @Param("now") LocalDateTime now,
      @Param("maxRetry") int maxRetry,
      @Param("pendingStatus") OutboxStatus pendingStatus,
      @Param("failedStatus") OutboxStatus failedStatus);
}
