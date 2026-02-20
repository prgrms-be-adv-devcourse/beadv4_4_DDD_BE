package com.modeunsa.boundedcontext.content.in.listener;

import com.modeunsa.boundedcontext.content.app.ContentFacade;
import com.modeunsa.boundedcontext.content.app.dto.member.ContentMemberDto;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ContentEventListener {

  private final ContentFacade contentFacade;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleMemberCreateEvent(MemberSignupEvent memberSignupEvent) {
    ContentMemberDto member =
        new ContentMemberDto(
            memberSignupEvent.memberId(), memberSignupEvent.email(), memberSignupEvent.realName());
    contentFacade.syncContentMember(member);
  }
}
