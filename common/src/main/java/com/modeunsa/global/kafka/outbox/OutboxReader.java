package com.modeunsa.global.kafka.outbox;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OutboxReader {

  // Pending 상태의 OutboxEventView 데이터 조회
  List<? extends OutboxEventView> findPendingEventsWithLock(Pageable pageable);

  // 특정 시간 이전에 생성된 OutboxEventView 데이터의 ID 조회 (SENT 상태)
  List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable);
}
