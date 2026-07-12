package com.modeunsa.boundedcontext.member.out;

import com.modeunsa.boundedcontext.member.domain.entity.MemberOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxReader;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface MemberOutboxReader extends OutboxReader {
  @Override
  List<MemberOutboxEvent> findPendingEventsWithLock(Pageable pageable);

  @Override
  List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable);
}
