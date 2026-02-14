package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentOutboxReader {
  List<PaymentOutboxEvent> findOutboxEventPage(PaymentOutboxStatus status, Pageable pageable);

  List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable);
}
