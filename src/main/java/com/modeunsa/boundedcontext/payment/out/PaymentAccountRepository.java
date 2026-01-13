package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {}
