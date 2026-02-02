package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.shared.member.event.MemberBasicInfoUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberBasicInfoUpdateUseCase {
  private final MemberSupport memberSupport;
  private final EventPublisher eventPublisher;

  public void execute(Long memberId, MemberBasicInfoUpdateRequest request) {
    Member member = memberSupport.getMember(memberId);

    member
        .updateRealName(request.getRealName())
        .updatePhoneNumber(request.getPhoneNumber())
        .updateEmail(request.getEmail());

    eventPublisher.publish(
        new MemberBasicInfoUpdatedEvent(
            memberId, member.getRealName(), member.getPhoneNumber(), member.getEmail()));
  }
}
