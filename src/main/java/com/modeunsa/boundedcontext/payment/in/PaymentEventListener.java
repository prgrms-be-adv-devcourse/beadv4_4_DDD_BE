package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final PaymentFacade paymentFacade;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberCreateEvent(PaymentMemberCreatedEvent paymentMemberCreatedEvent) {
    Long memberId = paymentMemberCreatedEvent.getMemberId();
    log.info("PaymentMemberCreatedEvent 수신 - memberId: {}", memberId);

    try {
      paymentFacade.createPaymentAccount(memberId);
      log.info("PaymentAccount 생성 완료 - memberId: {}", memberId);
    } catch (Exception e) {
      log.error("PaymentAccount 생성 실패 - memberId: {}", memberId, e);
      throw e;
    }
  }
}
