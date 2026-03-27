package com.modeunsa.boundedcontext.settlement.out;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxStore;
import java.util.List;

public interface SettlementOutboxStore extends OutboxStore {
  SettlementOutboxEvent store(SettlementOutboxEvent newSettlementOutboxEvent);

  void flush();

  @Override
  long deleteAlreadySentEventByIds(List<Long> ids);

  @Override
  void markProcessing(Long id);

  @Override
  void markSent(Long id);

  @Override
  void markFailed(Long id, String errorMessage, int maxRetry);
}
