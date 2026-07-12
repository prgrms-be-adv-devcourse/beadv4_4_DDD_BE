package com.modeunsa.boundedcontext.member.out.persistence.outbox;

import com.modeunsa.boundedcontext.member.domain.entity.MemberOutboxEvent;
import com.modeunsa.boundedcontext.member.out.MemberOutboxReader;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaMemberOutboxReader implements MemberOutboxReader {

  private final MemberOutboxQueryRepository queryRepository;

  @Override
  public List<MemberOutboxEvent> findPendingEventsWithLock(Pageable pageable) {
    return queryRepository.findPendingEventsWithLock(pageable);
  }

  @Override
  public List<Long> findDeleteTargetIds(LocalDateTime before, Pageable pageable) {
    return queryRepository.findDeleteTargetIds(before, pageable);
  }
}
