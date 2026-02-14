package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentOutboxStatus;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxReader;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentOutboxReader implements PaymentOutboxReader {

  private final PaymentOutboxQueryRepository queryRepository;

  @Override
  public List<PaymentOutboxEvent> findOutboxEventPage(
      PaymentOutboxStatus status, Pageable pageable) {
    return queryRepository.getOutboxEventPageByStatus(status, pageable);
  }

  @Override
  public List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable) {
    return queryRepository.findDeleteTargetIds(before, pageable);
  }
}
