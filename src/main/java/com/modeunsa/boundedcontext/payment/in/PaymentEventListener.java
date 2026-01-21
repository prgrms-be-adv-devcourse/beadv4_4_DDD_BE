package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentOrderCanceledEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentPayoutCompletedEvent;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.shared.auth.event.MemberSignupEvent;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
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
  private final PaymentMapper paymentMapper;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberCreateEvent(MemberSignupEvent memberSignupEvent) {
    PaymentMemberDto member = paymentMapper.toPaymentMemberDto(memberSignupEvent);
    paymentFacade.createPaymentMember(member);
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberAccountCreateEvent(PaymentMemberCreatedEvent paymentMemberCreatedEvent) {
    paymentFacade.createPaymentAccount(paymentMemberCreatedEvent.memberId());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePaymentFailedEvent(PaymentFailedEvent paymentFailedEvent) {
    //    paymentFacade.refund(paymentFailedEvent.payment(), RefundEventType.PAYMENT_FAILED);
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderCanceledEvent(PaymentOrderCanceledEvent paymentOrderCanceledEvent) {
    PaymentDto payment = paymentMapper.toPaymentDto(paymentOrderCanceledEvent.order());
    paymentFacade.refund(payment, RefundEventType.ORDER_CANCELLED);
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePayoutCompletedEvent(PaymentPayoutCompletedEvent paymentPayoutCompletedEvent) {
    paymentFacade.completePayout(paymentPayoutCompletedEvent.payout());
  }
}
