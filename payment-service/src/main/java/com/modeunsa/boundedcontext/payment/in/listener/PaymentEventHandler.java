package com.modeunsa.boundedcontext.payment.in.listener;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberSyncRequest;
import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.app.inbox.PaymentInboxRecorder;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.order.event.RefundRequestedEvent;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import com.modeunsa.shared.payment.event.PaymentMemberCreatedEvent;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventHandler {

  private final PaymentInboxRecorder inboxRecorder;
  private final JsonConverter jsonConverter;
  private final PaymentMapper paymentMapper;
  private final PaymentFacade paymentFacade;

  public void handle(DomainEventEnvelope envelope) {

    inboxRecorder.recordOrThrowDuplicate(
        envelope.eventId(), envelope.topic(), envelope.payload(), envelope.traceId());

    switch (envelope.eventType()) {
      case MemberSignupEvent.EVENT_NAME -> {
        MemberSignupEvent event =
            jsonConverter.deserialize(envelope.payload(), MemberSignupEvent.class);
        PaymentMemberSyncRequest member = paymentMapper.toPaymentMemberSyncRequest(event);
        paymentFacade.createPaymentMember(member);
      }

      case PaymentMemberCreatedEvent.EVENT_NAME -> {
        PaymentMemberCreatedEvent event =
            jsonConverter.deserialize(envelope.payload(), PaymentMemberCreatedEvent.class);
        paymentFacade.createPaymentAccount(event.memberId());
      }

      case PaymentFailedEvent.EVENT_NAME -> {
        PaymentFailedEvent event =
            jsonConverter.deserialize(envelope.payload(), PaymentFailedEvent.class);
        paymentFacade.handlePaymentFailed(event);
      }

      case RefundRequestedEvent.EVENT_NAME -> {
        RefundRequestedEvent event =
            jsonConverter.deserialize(envelope.payload(), RefundRequestedEvent.class);
        PaymentOrderInfo orderInfo = paymentMapper.toPaymentOrderInfo(event.orderDto());
        paymentFacade.refund(orderInfo, RefundEventType.ORDER_CANCELLED);
      }

      case SettlementCompletedPayoutEvent.EVENT_NAME -> {
        SettlementCompletedPayoutEvent event =
            jsonConverter.deserialize(envelope.payload(), SettlementCompletedPayoutEvent.class);
        List<PaymentPayoutInfo> payouts = paymentMapper.toPaymentPayoutInfoList(event.payouts());
        paymentFacade.completePayout(payouts);
      }

      default -> {
        // ignore
      }
    }
  }
}
