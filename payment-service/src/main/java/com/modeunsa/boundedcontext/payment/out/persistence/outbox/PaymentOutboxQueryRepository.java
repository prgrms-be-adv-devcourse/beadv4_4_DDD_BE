package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentOutboxEvent.paymentOutboxEvent;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentOutboxQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<PaymentOutboxEvent> findPendingEvents(Pageable pageable) {

    BooleanBuilder where = new BooleanBuilder();
    where.and(eqStatus(OutboxStatus.PENDING));

    JPAQuery<PaymentOutboxEvent> contentQuery =
        this.queryFactory
            .selectFrom(paymentOutboxEvent)
            .where(where)
            .orderBy(paymentOutboxEvent.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    return contentQuery.fetch();
  }

  public List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable) {

    JPAQuery<Long> contentQuery =
        this.queryFactory
            .select(paymentOutboxEvent.id)
            .from(paymentOutboxEvent)
            .where(
                eqStatus(OutboxStatus.SENT),
                paymentOutboxEvent.sentAt.isNotNull(),
                beforeSentAt(before))
            .orderBy(paymentOutboxEvent.sentAt.asc(), paymentOutboxEvent.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    return contentQuery.fetch();
  }

  private BooleanExpression eqStatus(OutboxStatus status) {
    return status != null ? paymentOutboxEvent.status.eq(status) : null;
  }

  private BooleanExpression beforeSentAt(LocalDateTime before) {
    return before != null ? paymentOutboxEvent.sentAt.before(before) : null;
  }
}
