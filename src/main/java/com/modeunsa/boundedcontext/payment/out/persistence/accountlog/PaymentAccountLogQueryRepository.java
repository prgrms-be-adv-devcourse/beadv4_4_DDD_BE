package com.modeunsa.boundedcontext.payment.out.persistence.accountlog;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentAccountLog.paymentAccountLog;

import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountLogDto;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountSearchRequest;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

  public Page<PaymentAccountLogDto> getAccountLedgerPageBySearch(
      Long memberId, PaymentAccountSearchRequest condition) {
    BooleanBuilder where = new BooleanBuilder();
    where.and(eqMemberId(memberId));
    where.and(betweenCreatedAt(condition.from(), condition.to()));

    JPAQuery<PaymentAccountLogDto> contentQuery =
        this.queryFactory
            .select(Projections.constructor(PaymentAccountLogDto.class, paymentAccountLog))
            .from(paymentAccountLog)
            .where(where)
            .orderBy(paymentAccountLog.createdAt.desc())
            .limit(condition.pageable().getPageSize())
            .offset(condition.pageable().getOffset());

    List<PaymentAccountLogDto> content = contentQuery.fetch();

    Long total =
        this.queryFactory
            .select(paymentAccountLog.count())
            .from(paymentAccountLog)
            .where(where)
            .fetchOne();

    long totalCount = total != null ? total : 0;

    return new PageImpl<>(content, condition.pageable(), totalCount);
  }

  private BooleanExpression eqMemberId(Long memberId) {
    return memberId != null ? paymentAccountLog.paymentAccount.member.id.eq(memberId) : null;
  }

  private BooleanExpression betweenCreatedAt(LocalDateTime from, LocalDateTime to) {
    if (from == null || to == null) {
      return null;
    }

    return paymentAccountLog.createdAt.between(from, to);
  }
}
