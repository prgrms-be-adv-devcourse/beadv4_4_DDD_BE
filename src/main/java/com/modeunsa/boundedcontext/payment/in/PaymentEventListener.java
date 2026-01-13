package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final PaymentFacade paymentFacade;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberCreateEvent(PaymentMemberCreatedEvent paymentMemberCreatedEvent) {
    paymentFacade.createPaymentAccount(paymentMemberCreatedEvent.getMemberId());
  }
}
