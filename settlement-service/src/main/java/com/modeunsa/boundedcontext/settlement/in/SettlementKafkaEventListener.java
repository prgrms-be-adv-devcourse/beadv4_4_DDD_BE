package com.modeunsa.boundedcontext.settlement.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.order.event.OrderPurchaseConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.event-consumer.type", havingValue = "kafka")
@RequiredArgsConstructor
public class SettlementKafkaEventListener {
  private final SettlementFacade settlementFacade;
  private final JsonConverter jsonConverter;

  @KafkaListener(topics = "member-events", groupId = "settlement-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case "MemberSignupEvent" -> {
        MemberSignupEvent event =
            jsonConverter.deserialize(envelope.payload(), MemberSignupEvent.class);
        settlementFacade.syncMember(event.memberId(), event.role());
      }
      default -> {}
    }
  }

  @KafkaListener(topics = "order-events", groupId = "settlement-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case "OrderPurchaseConfirmedEvent" -> {
        OrderPurchaseConfirmedEvent event =
            jsonConverter.deserialize(envelope.payload(), OrderPurchaseConfirmedEvent.class);
        settlementFacade.collectCandidateItems(event.orderDto().getOrderId());
      }
      default -> {}
    }
  }
}
