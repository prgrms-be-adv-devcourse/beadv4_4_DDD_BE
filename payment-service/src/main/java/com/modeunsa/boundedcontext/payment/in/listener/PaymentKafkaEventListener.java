package com.modeunsa.boundedcontext.payment.in.listener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberSyncRequest;
import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@ConditionalOnProperty(name = "app.event-consumer.type", havingValue = "kafka")
@RequiredArgsConstructor
public class PaymentKafkaEventListener {

  private final PaymentFacade paymentFacade;
  private final PaymentMapper paymentMapper;
  private final JsonConverter jsonConverter;

  @KafkaListener(topics = "member-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
    switch (envelope.eventType()) {
      case "MemberSignupEvent" -> {
        MemberSignupEvent event =
            jsonConverter.deserialize(envelope.payload(), MemberSignupEvent.class);
        PaymentMemberSyncRequest member = paymentMapper.toPaymentMemberSyncRequest(event);
        paymentFacade.createPaymentMember(member);
      }
      default -> {
        // ignore
      }
    }
    ackAfterCommit(ack);
  }

  @KafkaListener(topics = "payment-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePaymentEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
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
    ackAfterCommit(ack);
  }

  @KafkaListener(topics = "order-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
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
    ackAfterCommit(ack);
  }

  @KafkaListener(topics = "settlement-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePayoutEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
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
    ackAfterCommit(ack);
  }

  private void ackAfterCommit(Acknowledgment ack) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            ack.acknowledge();
          }
        });
  }
}
