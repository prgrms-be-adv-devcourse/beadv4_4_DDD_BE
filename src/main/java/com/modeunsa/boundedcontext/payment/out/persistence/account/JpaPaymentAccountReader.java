package com.modeunsa.boundedcontext.payment.out.persistence.account;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentAccountReader implements PaymentAccountReader {

  private final PaymentAccountQueryRepository queryRepository;

  @Override
  public Optional<PaymentAccount> findByMemberId(Long memberId) {
    return queryRepository.findByMemberId(memberId);
  }

  @Override
  public Optional<PaymentAccount> findByMemberIdWithLock(Long memberId) {
    return queryRepository.findByMemberIdWithLock(memberId);
  }

  @Override
  public boolean existsByMemberId(Long memberId) {
    return queryRepository.existsByMemberId(memberId);
  }
}
