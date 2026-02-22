package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentOutboxEvent.paymentOutboxEvent;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
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
public class PaymentOutboxCommandRepository {

  private final EntityManager entityManager;

  public PaymentOutboxEvent store(PaymentOutboxEvent event) {
    entityManager.persist(event);
    return event;
  }

  public long deleteAlreadySentEventBefore(List<Long> ids) {
    return new JPADeleteClause(entityManager, paymentOutboxEvent)
        .where(paymentOutboxEvent.id.in(ids))
        .execute();
  }

  public void updateStatus(Long id, OutboxStatus status, LocalDateTime now) {
    new JPAUpdateClause(entityManager, paymentOutboxEvent)
        .set(paymentOutboxEvent.status, status)
        .set(paymentOutboxEvent.updatedAt, now)
        .where(paymentOutboxEvent.id.eq(id))
        .execute();
  }

  public void markSent(Long id, OutboxStatus status, LocalDateTime sentAt) {
    new JPAUpdateClause(entityManager, paymentOutboxEvent)
        .set(paymentOutboxEvent.status, status)
        .set(paymentOutboxEvent.sentAt, sentAt)
        .set(paymentOutboxEvent.updatedAt, sentAt)
        .where(paymentOutboxEvent.id.eq(id))
        .execute();
  }

  public void markFailed(Long id, String errorMessage, LocalDateTime now, int maxRetry) {
    var statusCase =
        new CaseBuilder()
            .when(paymentOutboxEvent.retryCount.add(1).goe(maxRetry))
            .then(Expressions.constant(OutboxStatus.FAILED))
            .otherwise(Expressions.constant(OutboxStatus.PENDING));

    new JPAUpdateClause(entityManager, paymentOutboxEvent)
        .set(paymentOutboxEvent.lastErrorMessage, errorMessage)
        .set(paymentOutboxEvent.updatedAt, now)
        .set(paymentOutboxEvent.retryCount, paymentOutboxEvent.retryCount.add(1))
        .set(paymentOutboxEvent.status, statusCase)
        .where(paymentOutboxEvent.id.eq(id))
        .execute();
  }
}
