package com.modeunsa.boundedcontext.payment.out.persistence.inbox;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentInboxEvent.paymentInboxEvent;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentInboxQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Long countByEventId(String eventId) {
    return this.queryFactory
        .select(paymentInboxEvent.count())
        .from(paymentInboxEvent)
        .where(eqEventId(eventId))
        .fetchOne();
  }

  private BooleanExpression eqEventId(String eventId) {
    return paymentInboxEvent.eventId.eq(eventId);
  }
}
