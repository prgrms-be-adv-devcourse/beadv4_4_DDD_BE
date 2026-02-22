package com.modeunsa.boundedcontext.payment.out.persistence.payment;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPayment.payment;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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

  private BooleanExpression eqPaymentId(PaymentId paymentId) {
    return paymentId != null ? payment.id.eq(paymentId) : null;
  }

  private BooleanExpression eqOrderNo(String orderNo) {
    return StringUtils.hasText(orderNo) ? payment.id.orderNo.eq(orderNo) : null;
  }
}
