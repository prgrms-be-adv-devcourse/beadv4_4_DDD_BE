package com.modeunsa.boundedcontext.payment.out.persistence.account;

import static com.modeunsa.boundedcontext.payment.domain.entity.QPaymentAccount.paymentAccount;
import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentAccountQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<PaymentAccount> findByMemberId(Long memberId) {
    return Optional.ofNullable(
        this.queryFactory
            .selectFrom(paymentAccount)
            .where(paymentAccount.member.id.eq(memberId))
            .fetchOne());
  }

  public Optional<PaymentAccount> findByMemberIdWithLock(Long memberId) {
    return Optional.ofNullable(
        this.queryFactory
            .selectFrom(paymentAccount)
            .where(paymentAccount.member.id.eq(memberId))
            .setLockMode(PESSIMISTIC_WRITE)
            .fetchOne());
  }
}
