package com.modeunsa.boundedcontext.inventory.in;

import com.modeunsa.boundedcontext.inventory.app.InventoryFacade;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import com.modeunsa.shared.order.event.OrderCancellationConfirmedEvent;
import com.modeunsa.shared.order.event.OrderPaidEvent;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "kafka")
@RequiredArgsConstructor
public class InventoryKafkaEventListener {
  private final InventoryFacade inventoryFacade;
  private final JsonConverter jsonConverter;

  @KafkaListener(topics = "member-events", groupId = "inventory-service")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope eventEnvelope) {
    if (eventEnvelope.eventType().equals("SellerRegisteredEvent")) {
      SellerRegisteredEvent event =
          jsonConverter.deserialize(eventEnvelope.payload(), SellerRegisteredEvent.class);
      inventoryFacade.registerSeller(
          event.memberSellerId(), event.businessName(), event.representativeName());
    }
  }

  @KafkaListener(topics = "product-events", groupId = "inventory-service")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleProductEvent(DomainEventEnvelope eventEnvelope) {
    if (eventEnvelope.eventType().equals("ProductCreatedEvent")) {
      ProductCreatedEvent event =
          jsonConverter.deserialize(eventEnvelope.payload(), ProductCreatedEvent.class);
      inventoryFacade.createProduct(event.productDto());
    }
  }

  @KafkaListener(topics = "order-events", groupId = "inventory-service")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOrderEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case OrderCancellationConfirmedEvent.EVENT_NAME -> {
        OrderCancellationConfirmedEvent event =
            jsonConverter.deserialize(envelope.payload(), OrderCancellationConfirmedEvent.class);
        inventoryFacade.releaseInventory(event.orderItemDto());
      }
      case OrderPaidEvent.EVENT_NAME -> {
        OrderPaidEvent event = jsonConverter.deserialize(envelope.payload(), OrderPaidEvent.class);
        inventoryFacade.decreaseStock(event.orderDto().getOrderItems());
      }
      default -> {}
    }
  }
}
