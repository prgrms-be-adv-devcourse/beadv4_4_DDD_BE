package com.modeunsa.boundedcontext.settlement.out.persistence.outbox;

import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.boundedcontext.settlement.out.SettlementOutboxReader;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSettlementOutboxReader implements SettlementOutboxReader {

  private final SettlementOutboxQueryRepository queryRepository;

  @Override
  public List<SettlementOutboxEvent> findPendingEventsWithLock(Pageable pageable) {
    return queryRepository.findPendingEventsWithLock(pageable);
  }

  @Override
  public List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable) {
    return queryRepository.findDeleteTargetIds(before, pageable);
  }
}
