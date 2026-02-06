package com.modeunsa.boundedcontext.payment.out.persistence.account;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {

  Optional<PaymentAccount> findByMemberId(Long memberId);
}
