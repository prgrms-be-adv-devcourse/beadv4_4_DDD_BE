package com.modeunsa.boundedcontext.payment.out.persistence.payment;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPayment.payment;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<Payment> findById(PaymentId paymentId) {
    return Optional.ofNullable(
        this.queryFactory.selectFrom(payment).where(payment.id.eq(paymentId)).fetchOne());
  }
}
