package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentOutboxEvent.paymentOutboxEvent;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentOutboxStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentOutboxQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Page<PaymentOutboxEvent> getOutboxEventPageByStatus(
      PaymentOutboxStatus status, Pageable pageable) {

    BooleanBuilder where = new BooleanBuilder();
    where.and(eqStatus(status));

    JPAQuery<PaymentOutboxEvent> contentQuery =
        this.queryFactory
            .selectFrom(paymentOutboxEvent)
            .where(where)
            .orderBy(paymentOutboxEvent.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    List<PaymentOutboxEvent> content = contentQuery.fetch();

    Long total =
        this.queryFactory
            .select(paymentOutboxEvent.count())
            .from(paymentOutboxEvent)
            .where(where)
            .fetchOne();

    long totalCount = total != null ? total : 0;

    return new PageImpl<>(content, pageable, totalCount);
  }

  public List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable) {

    JPAQuery<Long> contentQuery =
        this.queryFactory
            .select(paymentOutboxEvent.id)
            .from(paymentOutboxEvent)
            .where(
                eqStatus(PaymentOutboxStatus.SENT),
                paymentOutboxEvent.sentAt.isNotNull(),
                beforeSentAt(before))
            .orderBy(paymentOutboxEvent.sentAt.asc(), paymentOutboxEvent.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    return contentQuery.fetch();
  }

  private BooleanExpression eqStatus(PaymentOutboxStatus status) {
    return status != null ? paymentOutboxEvent.status.eq(status) : null;
  }

  private BooleanExpression beforeSentAt(LocalDateTime before) {
    return before != null ? paymentOutboxEvent.sentAt.before(before) : null;
  }
}
