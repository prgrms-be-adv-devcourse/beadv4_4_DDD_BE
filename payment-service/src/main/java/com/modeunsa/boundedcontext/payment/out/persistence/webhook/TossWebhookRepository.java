package com.modeunsa.boundedcontext.payment.out.persistence.webhook;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentTossWebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TossWebhookRepository extends JpaRepository<PaymentTossWebhookLog, Long> {}
