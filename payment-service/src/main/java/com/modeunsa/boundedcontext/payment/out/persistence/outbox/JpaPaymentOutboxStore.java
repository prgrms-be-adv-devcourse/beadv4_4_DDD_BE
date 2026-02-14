package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxStore;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentOutboxStore implements PaymentOutboxStore {

  private final PaymentOutboxRepository paymentOutboxRepository;

  @Override
  public PaymentOutboxEvent store(PaymentOutboxEvent newPaymentOutboxEvent) {
    return paymentOutboxRepository.save(newPaymentOutboxEvent);
  }

  @Override
  public int deleteAlreadySentEventByIds(List<Long> ids) {
    return paymentOutboxRepository.deleteAlreadySentEventBefore(ids);
  }
}
