package com.modeunsa.boundedcontext.member.in.listener;

import com.modeunsa.shared.member.event.MemberSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignupEventListener {

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMemberSignup(MemberSignupEvent event) {
    // TODO: 회원가입 후 처리 로직 구현 예정
    log.info("회원가입 이벤트 수신 - memberId: {}", event.memberId());
  }
}
