package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.shared.auth.event.MemberSignupEvent;
import com.modeunsa.shared.order.event.RefundRequestedEvent;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import java.util.List;
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
    paymentFacade.changeStatusToFailed(paymentFailedEvent);
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleRefundRequestEvent(RefundRequestedEvent refundRequestedEvent) {
    PaymentOrderInfo orderInfo = paymentMapper.toPaymentOrderInfo(refundRequestedEvent.orderDto());
    paymentFacade.refund(orderInfo, RefundEventType.ORDER_CANCELLED);
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePayoutCompletedEvent(
      SettlementCompletedPayoutEvent settlementCompletedPayoutEvent) {
    List<PaymentPayoutInfo> payouts =
        paymentMapper.toPaymentPayoutInfoList(settlementCompletedPayoutEvent.payouts());
    paymentFacade.completePayout(payouts);
  }
}
