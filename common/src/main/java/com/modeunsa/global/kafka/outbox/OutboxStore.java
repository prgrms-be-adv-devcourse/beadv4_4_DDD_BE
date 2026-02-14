package com.modeunsa.global.kafka.outbox;

import java.util.List;

public interface OutboxStore {

  void markProcessing(Long id);

  void markSent(Long id);

  void markFailed(Long id, String errorMessage, int maxRetry);

  // 이미 SENT 상태로 마킹된 OutboxEventView 데이터를 삭제
  int deleteAlreadySentEventByIds(List<Long> ids);
}
