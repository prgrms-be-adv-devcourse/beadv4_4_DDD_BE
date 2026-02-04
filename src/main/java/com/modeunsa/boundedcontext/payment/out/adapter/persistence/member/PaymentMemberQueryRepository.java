package com.modeunsa.boundedcontext.payment.out.adapter.persistence.member;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentMember.paymentMember;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentMemberQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<PaymentMember> findById(Long memberId) {
    return Optional.ofNullable(
        this.queryFactory
            .selectFrom(paymentMember)
            .where(paymentMember.id.eq(memberId))
            .fetchOne());
  }
}
