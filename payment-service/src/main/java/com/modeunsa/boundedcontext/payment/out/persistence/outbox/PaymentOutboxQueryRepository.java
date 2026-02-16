package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentOutboxEvent.paymentOutboxEvent;
import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

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

  public static final String LOCK_TIMEOUT = "jakarta.persistence.lock.timeout";
  public static final int SKIP_LOCKED = -2;

  private final JPAQueryFactory queryFactory;

  // polling 주기를 고려하여, createdAt 기준으로 내림차순 정렬하여 가장 오래된 PENDING 이벤트부터 처리하도록 함
  // PESSIMISTIC_WRITE 락과 SKIP_LOCKED 힌트를 사용하여 동시에 여러 인스턴스에서 실행되는 경우에도 중복 처리가 발생하지 않도록 함
  public List<PaymentOutboxEvent> findPendingEventsWithLock(Pageable pageable) {

    BooleanBuilder where = new BooleanBuilder();
    where.and(eqStatus(OutboxStatus.PENDING));

    JPAQuery<PaymentOutboxEvent> query =
        this.queryFactory
            .selectFrom(paymentOutboxEvent)
            .where(where)
            .orderBy(paymentOutboxEvent.createdAt.desc())
            .setLockMode(PESSIMISTIC_WRITE)
            .setHint(LOCK_TIMEOUT, SKIP_LOCKED)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    return query.fetch();
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
