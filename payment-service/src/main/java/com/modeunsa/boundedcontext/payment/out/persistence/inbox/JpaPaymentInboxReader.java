package com.modeunsa.boundedcontext.payment.out.persistence.inbox;

import com.modeunsa.boundedcontext.payment.out.PaymentInboxReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentInboxReader implements PaymentInboxReader {

  private final PaymentInboxQueryRepository queryRepository;

  @Override
  public boolean existsByEventId(String eventId) {
    return queryRepository.countByEventId(eventId) > 0;
  }
}
