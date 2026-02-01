package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.order.event.RefundRequestedEvent;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentKafkaEventListener {

  private final PaymentFacade paymentFacade;
  private final PaymentMapper paymentMapper;
  private final JsonConverter jsonConverter;

  @KafkaListener(topics = "member-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case "MemberSignupEvent" -> {
        MemberSignupEvent event =
            jsonConverter.deserialize(envelope.payload(), MemberSignupEvent.class);
        PaymentMemberDto member = paymentMapper.toPaymentMemberDto(event);
        paymentFacade.createPaymentMember(member);
      }
      default -> {
        // ignore}
      }
    }
  }

  @KafkaListener(topics = "payment-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePaymentEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
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
      default -> {
        // ignore
      }
    }
  }

  @KafkaListener(topics = "order-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case "RefundRequestedEvent" -> {
        RefundRequestedEvent event =
            jsonConverter.deserialize(envelope.payload(), RefundRequestedEvent.class);
        PaymentOrderInfo orderInfo = paymentMapper.toPaymentOrderInfo(event.orderDto());
        paymentFacade.refund(orderInfo, RefundEventType.ORDER_CANCELLED);
      }
      default -> {
        // ignore
      }
    }
  }

  @KafkaListener(topics = "settlement-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePayoutEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case "SettlementCompletedPayoutEvent" -> {
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
