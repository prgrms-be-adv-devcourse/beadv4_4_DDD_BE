package com.modeunsa.boundedcontext.order.in;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.member.event.MemberBasicInfoUpdatedEvent;
import com.modeunsa.shared.member.event.MemberDeliveryAddressSetAsDefaultEvent;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.payment.event.PaymentFinalFailureEvent;
import com.modeunsa.shared.payment.event.PaymentRefundSuccessEvent;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import com.modeunsa.shared.product.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "kafka")
@RequiredArgsConstructor
public class OrderKafkaEventListener {

  private final OrderFacade orderFacade;
  private final JsonConverter jsonConverter;

  @KafkaListener(topics = "member-events", groupId = "order-service")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope eventEnvelope) {
    switch (eventEnvelope.eventType()) {
      case "MemberSignUpEvent" -> {
        MemberSignupEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), MemberSignupEvent.class);
        orderFacade.syncMember(event.memberId(), event.realName(), event.phoneNumber());
      }
      case "MemberBasicInfoUpdatedEvent" -> {
        MemberBasicInfoUpdatedEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), MemberBasicInfoUpdatedEvent.class);
        orderFacade.updateMember(event.memberId(), event.realName(), event.phoneNumber());
      }
      case "MemberDeliveryAddressSetAsDefaultEvent" -> {
        MemberDeliveryAddressSetAsDefaultEvent event =
            jsonConverter.deserialize(
                eventEnvelope.payload(), MemberDeliveryAddressSetAsDefaultEvent.class);
        orderFacade.createDeliveryAddress(
            event.memberId(),
            event.recipientName(),
            event.recipientPhone(),
            event.zipCode(),
            event.address(),
            event.addressDetail(),
            event.addressName());
      }
      default -> {}
    }
  }

  @KafkaListener(topics = "product-events", groupId = "order-service")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleProductEvent(DomainEventEnvelope eventEnvelope) {
    switch (eventEnvelope.eventType()) {
      case "ProductCreatedEvent" -> {
        ProductCreatedEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), ProductCreatedEvent.class);
        orderFacade.createProduct(event.productDto());
      }
      case "ProductUpdatedEvent" -> {
        ProductUpdatedEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), ProductUpdatedEvent.class);
        orderFacade.updateProduct(event.productDto());
      }
      default -> {}
    }
  }

  @KafkaListener(topics = "payment-events", groupId = "order-service")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handlePaymentEvent(DomainEventEnvelope eventEnvelope) {
    switch (eventEnvelope.eventType()) {
      case PaymentSuccessEvent.EVENT_NAME -> {
        PaymentSuccessEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), PaymentSuccessEvent.class);
        orderFacade.approveOrder(event.payment());
      }
      case PaymentFinalFailureEvent.EVENT_NAME -> {
        PaymentFinalFailureEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), PaymentFinalFailureEvent.class);
        orderFacade.rejectOrder(event.payment());
      }
      case PaymentRefundSuccessEvent.EVENT_NAME -> {
        PaymentRefundSuccessEvent event =
            jsonConverter.deserialize(eventEnvelope.payload(), PaymentRefundSuccessEvent.class);
        orderFacade.confirmOrderCancellation(event.payment());
      }
      default -> {}
    }
  }
}
