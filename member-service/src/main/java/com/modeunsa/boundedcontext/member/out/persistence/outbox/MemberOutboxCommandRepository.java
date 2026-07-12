package com.modeunsa.boundedcontext.member.out.persistence.outbox;

import static com.modeunsa.boundedcontext.member.domain.entity.QMemberOutboxEvent.memberOutboxEvent;

import com.modeunsa.boundedcontext.member.domain.entity.MemberOutboxEvent;
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
public class MemberOutboxCommandRepository {

  private final EntityManager entityManager;

  public MemberOutboxEvent store(MemberOutboxEvent event) {
    entityManager.persist(event);
    return event;
  }

  public long deleteAlreadySentEventBefore(List<Long> ids) {
    return new JPADeleteClause(entityManager, memberOutboxEvent)
        .where(memberOutboxEvent.id.in(ids))
        .execute();
  }

  public void updateStatus(Long id, OutboxStatus status, LocalDateTime now) {
    new JPAUpdateClause(entityManager, memberOutboxEvent)
        .set(memberOutboxEvent.status, status)
        .set(memberOutboxEvent.updatedAt, now)
        .where(memberOutboxEvent.id.eq(id))
        .execute();
  }

  public void markSent(Long id, OutboxStatus status, LocalDateTime sentAt) {
    new JPAUpdateClause(entityManager, memberOutboxEvent)
        .set(memberOutboxEvent.status, status)
        .set(memberOutboxEvent.sentAt, sentAt)
        .set(memberOutboxEvent.updatedAt, sentAt)
        .where(memberOutboxEvent.id.eq(id))
        .execute();
  }

  public void markFailed(Long id, String errorMessage, LocalDateTime now, int maxRetry) {
    var statusCase =
        new CaseBuilder()
            .when(memberOutboxEvent.retryCount.add(1).goe(maxRetry))
            .then(Expressions.constant(OutboxStatus.FAILED))
            .otherwise(Expressions.constant(OutboxStatus.PENDING));

    new JPAUpdateClause(entityManager, memberOutboxEvent)
        .set(memberOutboxEvent.lastErrorMessage, errorMessage)
        .set(memberOutboxEvent.updatedAt, now)
        .set(memberOutboxEvent.retryCount, memberOutboxEvent.retryCount.add(1))
        .set(memberOutboxEvent.status, statusCase)
        .where(memberOutboxEvent.id.eq(id))
        .execute();
  }
}
