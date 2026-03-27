package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxReader;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface SettlementOutboxReader extends OutboxReader {
  @Override
  List<SettlementOutboxEvent> findPendingEventsWithLock(Pageable pageable);

  @Override
  List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable);
}
