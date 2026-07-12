package com.modeunsa.boundedcontext.member.out;

import com.modeunsa.boundedcontext.member.domain.entity.MemberOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxStore;
import java.util.List;

public interface MemberOutboxStore extends OutboxStore {
  MemberOutboxEvent store(MemberOutboxEvent newMemberOutboxEvent);

  @Override
  long deleteAlreadySentEventByIds(List<Long> ids);

  @Override
  void markProcessing(Long id);

  @Override
  void markSent(Long id);

  @Override
  void markFailed(Long id, String errorMessage, int maxRetry);
}
