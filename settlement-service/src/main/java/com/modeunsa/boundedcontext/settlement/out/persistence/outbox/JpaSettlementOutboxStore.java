package com.modeunsa.boundedcontext.settlement.out.persistence.outbox;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.boundedcontext.settlement.out.SettlementOutboxStore;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSettlementOutboxStore implements SettlementOutboxStore {

  private final SettlementOutboxCommandRepository settlementOutboxCommandRepository;

  @Override
  public SettlementOutboxEvent store(SettlementOutboxEvent newSettlementOutboxEvent) {
    return settlementOutboxCommandRepository.store(newSettlementOutboxEvent);
  }

  @Override
  public void flush() {
    settlementOutboxCommandRepository.flush();
  }

  @Override
  public long deleteAlreadySentEventByIds(List<Long> ids) {
    return settlementOutboxCommandRepository.deleteAlreadySentEventBefore(ids);
  }

  @Override
  public void markProcessing(Long id) {
    settlementOutboxCommandRepository.updateStatus(
        id, OutboxStatus.PROCESSING, LocalDateTime.now());
  }

  @Override
  public void markSent(Long id) {
    settlementOutboxCommandRepository.markSent(id, OutboxStatus.SENT, LocalDateTime.now());
  }

  @Override
  public void markFailed(Long id, String errorMessage, int maxRetry) {
    settlementOutboxCommandRepository.markFailed(id, errorMessage, LocalDateTime.now(), maxRetry);
  }
}
