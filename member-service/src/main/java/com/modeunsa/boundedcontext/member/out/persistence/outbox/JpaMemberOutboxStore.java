package com.modeunsa.boundedcontext.member.out.persistence.outbox;

import com.modeunsa.boundedcontext.member.domain.entity.MemberOutboxEvent;
import com.modeunsa.boundedcontext.member.out.MemberOutboxStore;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaMemberOutboxStore implements MemberOutboxStore {

  private final MemberOutboxCommandRepository memberOutboxCommandRepository;

  @Override
  public MemberOutboxEvent store(MemberOutboxEvent newMemberOutboxEvent) {
    return memberOutboxCommandRepository.store(newMemberOutboxEvent);
  }

  @Override
  public long deleteAlreadySentEventByIds(List<Long> ids) {
    return memberOutboxCommandRepository.deleteAlreadySentEventBefore(ids);
  }

  @Override
  public void markProcessing(Long id) {
    memberOutboxCommandRepository.updateStatus(id, OutboxStatus.PROCESSING, LocalDateTime.now());
  }

  @Override
  public void markSent(Long id) {
    memberOutboxCommandRepository.markSent(id, OutboxStatus.SENT, LocalDateTime.now());
  }

  @Override
  public void markFailed(Long id, String errorMessage, int maxRetry) {
    memberOutboxCommandRepository.markFailed(id, errorMessage, LocalDateTime.now(), maxRetry);
  }
}
