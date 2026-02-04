package com.modeunsa.boundedcontext.payment.out.persistence;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import java.util.Optional;

public interface PaymentAccountReader {
  Optional<PaymentAccount> findByMemberId(Long memberId);

  Optional<PaymentAccount> findByMemberIdWithLock(Long memberId);
}
