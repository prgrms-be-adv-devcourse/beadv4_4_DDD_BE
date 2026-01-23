package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccountLog;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAccountLogRepository extends JpaRepository<PaymentAccountLog, Long> {
  long countByReferenceType(ReferenceType referenceType);
}
