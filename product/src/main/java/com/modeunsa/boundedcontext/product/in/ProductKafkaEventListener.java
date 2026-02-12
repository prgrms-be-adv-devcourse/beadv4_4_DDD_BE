package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.member.event.MemberBasicInfoUpdatedEvent;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "kafka")
@RequiredArgsConstructor
public class ProductKafkaEventListener {

  private final ProductFacade productFacade;
  private final JsonConverter jsonConverter;

  @KafkaListener(topics = "member-events", groupId = "product-service")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope eventEnvelope) {
    switch (eventEnvelope.eventType()) {
      case "MemberSignUpEvent" -> {
        MemberSignupEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), MemberSignupEvent.class);
        productFacade.syncMember(
            event.memberId(), event.email(), event.realName(), event.phoneNumber());
      }
      case "MemberBasicInfoUpdatedEvent" -> {
        MemberBasicInfoUpdatedEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), MemberBasicInfoUpdatedEvent.class);
        productFacade.updateMember(
            event.memberId(), event.realName(), event.email(), event.phoneNumber());
      }
      case "SellerRegisteredEvent" -> {
        SellerRegisteredEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), SellerRegisteredEvent.class);
        productFacade.syncSeller(
            event.memberSellerId(),
            event.memberId(),
            event.businessName(),
            event.representativeName());
      }
      default -> {}
    }
  }
}
