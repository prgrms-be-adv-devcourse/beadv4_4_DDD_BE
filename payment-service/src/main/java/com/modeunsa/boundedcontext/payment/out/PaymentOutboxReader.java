package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxReader;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentOutboxReader extends OutboxReader {
  @Override
  List<PaymentOutboxEvent> findPendingEvents(Pageable pageable);

  @Override
  List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable);
}
