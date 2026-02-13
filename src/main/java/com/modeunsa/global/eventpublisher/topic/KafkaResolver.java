package com.modeunsa.global.eventpublisher.topic;

import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.shared.inventory.event.InventoryStockRecoverEvent;
import com.modeunsa.shared.member.event.MemberBasicInfoUpdatedEvent;
import com.modeunsa.shared.member.event.MemberDeliveryAddressAddedEvent;
import com.modeunsa.shared.member.event.MemberDeliveryAddressDeletedEvent;
import com.modeunsa.shared.member.event.MemberDeliveryAddressSetAsDefaultEvent;
import com.modeunsa.shared.member.event.MemberDeliveryAddressUpdatedEvent;
import com.modeunsa.shared.member.event.MemberProfileCreatedEvent;
import com.modeunsa.shared.member.event.MemberProfileUpdatedEvent;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import com.modeunsa.shared.order.event.OrderCancelRequestEvent;
import com.modeunsa.shared.order.event.OrderCancellationConfirmedEvent;
import com.modeunsa.shared.order.event.OrderPaidEvent;
import com.modeunsa.shared.order.event.OrderPurchaseConfirmedEvent;
import com.modeunsa.shared.order.event.RefundRequestedEvent;
import com.modeunsa.shared.payment.event.PaymentFinalFailureEvent;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import com.modeunsa.shared.product.event.ProductUpdatedEvent;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "kafka")
public class KafkaResolver {

  private static final String MEMBER_EVENTS_TOPIC = "member-events";
  private static final String PAYMENT_EVENTS_TOPIC = "payment-events";
  private static final String ORDER_EVENTS_TOPIC = "order-events";
  private static final String SETTLEMENT_EVENTS_TOPIC = "settlement-events";
  private static final String PRODUCT_EVENTS_TOPIC = "product-events";

  public String resolveTopic(Object event) {

    if (event instanceof MemberSignupEvent
        || event instanceof MemberBasicInfoUpdatedEvent
        || event instanceof MemberProfileCreatedEvent
        || event instanceof MemberProfileUpdatedEvent
        || event instanceof MemberDeliveryAddressAddedEvent
        || event instanceof MemberDeliveryAddressUpdatedEvent
        || event instanceof MemberDeliveryAddressDeletedEvent
        || event instanceof MemberDeliveryAddressSetAsDefaultEvent
        || event instanceof SellerRegisteredEvent) {
      return MEMBER_EVENTS_TOPIC;
    }

    // payment
    if (event instanceof PaymentMemberCreatedEvent
        || event instanceof PaymentFailedEvent
        || event instanceof PaymentSuccessEvent
        || event instanceof PaymentFinalFailureEvent) {
      return PAYMENT_EVENTS_TOPIC;
    }

    // order
    if (event instanceof OrderPaidEvent
        || event instanceof OrderPurchaseConfirmedEvent
        || event instanceof OrderCancelRequestEvent
        || event instanceof RefundRequestedEvent
        || event instanceof OrderCancellationConfirmedEvent) {
      return ORDER_EVENTS_TOPIC;
    }

    // settlement
    if (event instanceof SettlementCompletedPayoutEvent) {
      return SETTLEMENT_EVENTS_TOPIC;
    }

    // product
    if (event instanceof ProductCreatedEvent) {
      return PRODUCT_EVENTS_TOPIC;
    }
    if (event instanceof ProductUpdatedEvent) {
      return PRODUCT_EVENTS_TOPIC;
    }

    return "unexpected-events-topic";
  }

  // key 는 같은 topic 안에서 동일한 key 라면 같은 파티션에서 순차적으로 메시지가 처리된다.
  public String resolveKey(Object event) {
    if (event instanceof MemberSignupEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof MemberBasicInfoUpdatedEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof MemberProfileCreatedEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof MemberProfileUpdatedEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof MemberDeliveryAddressAddedEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof MemberDeliveryAddressUpdatedEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof MemberDeliveryAddressDeletedEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof MemberDeliveryAddressSetAsDefaultEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof SellerRegisteredEvent e) {
      return "member-%d-seller-%d".formatted(e.memberId(), e.memberSellerId());
    }

    if (event instanceof OrderPaidEvent e) {
      return "order-%d".formatted(e.orderDto().getOrderId());
    }
    if (event instanceof OrderCancelRequestEvent e) {
      return "order-%d".formatted(e.orderDto().getOrderId());
    }
    if (event instanceof OrderCancellationConfirmedEvent e) {
      return "order-%d".formatted(e.orderId());
    }
    if (event instanceof OrderPurchaseConfirmedEvent e) {
      return "order-%d".formatted(e.orderDto().getOrderId());
    }
    if (event instanceof RefundRequestedEvent e) {
      return "order-%d".formatted(e.orderDto().getOrderId());
    }

    if (event instanceof InventoryStockRecoverEvent e) {
      return "inventory-%d".formatted(e.orderItems().get(0).getProductId());
    }

    if (event instanceof PaymentMemberCreatedEvent e) {
      return "payment-member-%d".formatted(e.memberId());
    }
    if (event instanceof PaymentFailedEvent e) {
      return "payment-%d-%s".formatted(e.memberId(), e.orderNo());
    }

    if (event instanceof SettlementCompletedPayoutEvent e) {
      return "settlement";
    }

    if (event instanceof ProductCreatedEvent e) {
      return "product-%d".formatted(e.productDto().getId());
    }
    if (event instanceof ProductUpdatedEvent e) {
      return "product-%d".formatted(e.productDto().getId());
    }

    return "unexpected-key";
  }
}
