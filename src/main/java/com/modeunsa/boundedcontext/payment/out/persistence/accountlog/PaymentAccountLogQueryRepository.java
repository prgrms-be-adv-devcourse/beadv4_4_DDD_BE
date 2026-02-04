package com.modeunsa.boundedcontext.payment.out.persistence.accountlog;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentAccountLog.paymentAccountLog;

import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentAccountLogQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Long countByReferenceType(ReferenceType referenceType) {
    return this.queryFactory
        .select(paymentAccountLog.count())
        .from(paymentAccountLog)
        .where(paymentAccountLog.referenceType.eq(referenceType))
        .fetchOne();
  }
}
