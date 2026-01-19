package com.modeunsa.boundedcontext.settlement.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.app.event.SettlementMemberEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Configuration
@RequiredArgsConstructor
public class SettlementEventListener {
  private final SettlementFacade settlementFacade;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(SettlementMemberEvent event) {
    settlementFacade.syncMember(event.memberId(), event.memberRole());
  }
}
