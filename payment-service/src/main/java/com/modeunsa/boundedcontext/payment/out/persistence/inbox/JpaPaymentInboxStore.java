package com.modeunsa.boundedcontext.payment.out.persistence.inbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentInboxEvent;
import com.modeunsa.boundedcontext.payment.out.PaymentInboxStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentInboxStore implements PaymentInboxStore {

  private final PaymentInboxRepository paymentInboxRepository;

  @Override
  public PaymentInboxEvent store(PaymentInboxEvent newPaymentInboxEvent) {
    return paymentInboxRepository.save(newPaymentInboxEvent);
  }
}
