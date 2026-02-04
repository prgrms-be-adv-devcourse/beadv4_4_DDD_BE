package com.modeunsa.boundedcontext.payment.out.adapter.persistence.accountlog;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccountLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAccountLogRepository extends JpaRepository<PaymentAccountLog, Long> {}
