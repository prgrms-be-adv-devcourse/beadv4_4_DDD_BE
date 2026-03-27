package com.modeunsa.boundedcontext.settlement.out.persistence.outbox;

import static com.modeunsa.boundedcontext.settlement.domain.entity.QSettlementOutboxEvent.settlementOutboxEvent;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SettlementOutboxCommandRepository {

  private final EntityManager entityManager;

  public SettlementOutboxEvent store(SettlementOutboxEvent event) {
    entityManager.persist(event);
    return event;
  }

  public void flush() {
    entityManager.flush();
  }

  public long deleteAlreadySentEventBefore(List<Long> ids) {
    return new JPADeleteClause(entityManager, settlementOutboxEvent)
        .where(settlementOutboxEvent.id.in(ids))
        .execute();
  }

  public void updateStatus(Long id, OutboxStatus status, LocalDateTime now) {
    new JPAUpdateClause(entityManager, settlementOutboxEvent)
        .set(settlementOutboxEvent.status, status)
        .set(settlementOutboxEvent.updatedAt, now)
        .where(settlementOutboxEvent.id.eq(id))
        .execute();
  }

  public void markSent(Long id, OutboxStatus status, LocalDateTime sentAt) {
    new JPAUpdateClause(entityManager, settlementOutboxEvent)
        .set(settlementOutboxEvent.status, status)
        .set(settlementOutboxEvent.sentAt, sentAt)
        .set(settlementOutboxEvent.updatedAt, sentAt)
        .where(settlementOutboxEvent.id.eq(id))
        .execute();
  }

  public void markFailed(Long id, String errorMessage, LocalDateTime now, int maxRetry) {
    var statusCase =
        new CaseBuilder()
            .when(settlementOutboxEvent.retryCount.add(1).goe(maxRetry))
            .then(Expressions.constant(OutboxStatus.FAILED))
            .otherwise(Expressions.constant(OutboxStatus.PENDING));

    new JPAUpdateClause(entityManager, settlementOutboxEvent)
        .set(settlementOutboxEvent.lastErrorMessage, errorMessage)
        .set(settlementOutboxEvent.updatedAt, now)
        .set(settlementOutboxEvent.retryCount, settlementOutboxEvent.retryCount.add(1))
        .set(settlementOutboxEvent.status, statusCase)
        .where(settlementOutboxEvent.id.eq(id))
        .execute();
  }
}
