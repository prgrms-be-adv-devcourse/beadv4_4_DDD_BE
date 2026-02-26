package com.modeunsa.boundedcontext.payment.out.persistence.payment;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPayment.payment;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentListItemResponse;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentSearchRequest;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class PaymentQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<Payment> findById(PaymentId paymentId) {
    return Optional.ofNullable(
        this.queryFactory.selectFrom(payment).where(eqPaymentId(paymentId)).fetchOne());
  }

  public Optional<Payment> findByOrderNo(String orderNo) {
    return Optional.ofNullable(
        this.queryFactory.selectFrom(payment).where(eqOrderNo(orderNo)).fetchOne());
  }

  public Page<PaymentListItemResponse> findPageByMemberIdWithSearch(
      Long memberId, PaymentSearchRequest condition) {
    BooleanBuilder where = new BooleanBuilder();
    where.and(eqMemberId(memberId));
    where.and(betweenCreatedAt(condition.from(), condition.to()));
    where.and(eqStatus(condition.status()));
    where.and(eqOrderNo(condition.orderNo()));
    where.and(eqPaymentProvider(condition.paymentProvider()));

    Pageable pageable = condition.pageable();
    JPAQuery<PaymentListItemResponse> contentQuery =
        this.queryFactory
            .select(
                Projections.constructor(
                    PaymentListItemResponse.class,
                    payment.id.orderNo,
                    payment.orderId,
                    payment.status,
                    payment.totalAmount,
                    payment.pgOrderName,
                    payment.paymentProvider,
                    payment.createdAt))
            .from(payment)
            .where(where)
            .orderBy(payment.createdAt.desc())
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset());

    List<PaymentListItemResponse> content = contentQuery.fetch();

    Long total = this.queryFactory.select(payment.count()).from(payment).where(where).fetchOne();
    long totalCount = total != null ? total : 0;

    return new PageImpl<>(content, pageable, totalCount);
  }

  public Optional<Payment> findByPgOrderId(String orderId) {
    return Optional.ofNullable(
        this.queryFactory.selectFrom(payment).where(payment.pgOrderId.eq(orderId)).fetchOne());
  }

  private BooleanExpression eqPaymentId(PaymentId paymentId) {
    return paymentId != null ? payment.id.eq(paymentId) : null;
  }

  private BooleanExpression eqMemberId(Long memberId) {
    return memberId != null ? payment.id.memberId.eq(memberId) : null;
  }

  private BooleanExpression eqOrderNo(String orderNo) {
    return StringUtils.hasText(orderNo) ? payment.id.orderNo.eq(orderNo) : null;
  }

  private BooleanExpression betweenCreatedAt(LocalDateTime from, LocalDateTime to) {
    if (from == null || to == null) {
      return null;
    }
    return payment.createdAt.between(from, to);
  }

  private BooleanExpression eqStatus(PaymentStatus status) {
    return status != null ? payment.status.eq(status) : null;
  }

  private BooleanExpression eqPaymentProvider(ProviderType paymentProvider) {
    return paymentProvider != null ? payment.paymentProvider.eq(paymentProvider) : null;
  }
}
