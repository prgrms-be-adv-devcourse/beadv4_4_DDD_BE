package com.modeunsa.boundedcontext.payment.out.persistence.inbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentInboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentInboxRepository extends JpaRepository<PaymentInboxEvent, Long> {}
