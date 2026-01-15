package com.modeunsa.boundedcontext.settlement.app.usecase;

import com.modeunsa.boundedcontext.settlement.app.event.SettlementMemberCreatedEvent;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementSyncMemberUseCase {
  private final SettlementMemberRepository settlementMemberRepository;
  private final SpringDomainEventPublisher eventPublisher;

  public SettlementMember syncMember(Long memberId, String memberRole) {
    SettlementMember settlementMember = SettlementMember.create(memberId, memberRole);

    settlementMemberRepository.save(settlementMember);

    eventPublisher.publish(new SettlementMemberCreatedEvent(settlementMember.toDto()));

    return settlementMember;
  }
}
