package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
  boolean existsByMemberId(Long memberId);

  Optional<PaymentAccount> findByMemberId(Long memberId);
}
