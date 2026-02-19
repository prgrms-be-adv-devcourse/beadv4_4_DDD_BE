package com.modeunsa.global.eventpublisher.topic;

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
import com.modeunsa.shared.order.event.OrderPurchaseConfirmedEvent;
import com.modeunsa.shared.order.event.RefundRequestedEvent;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import com.modeunsa.shared.payment.event.PaymentFinalFailureEvent;
import com.modeunsa.shared.payment.event.PaymentMemberCreatedEvent;
import com.modeunsa.shared.payment.event.PaymentRefundSuccessEvent;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import com.modeunsa.shared.product.event.ProductOrderAvailabilityChangedEvent;
import com.modeunsa.shared.product.event.ProductUpdatedEvent;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression(
    "'${app.event-publisher.type}' == 'kafka' or '${app.event-publisher.type}' == 'outbox'")
public class KafkaResolver {

  private static final String MEMBER_EVENTS_TOPIC = "member-events";
  private static final String PAYMENT_EVENTS_TOPIC = "payment-events";
  private static final String ORDER_EVENTS_TOPIC = "order-events";
  private static final String SETTLEMENT_EVENTS_TOPIC = "settlement-events";
  private static final String PRODUCT_EVENTS_TOPIC = "product-events";

  // TODO: topic Resolver 를 각 모듈로 변경할지, kafka 에서 관리할지 고민
  public KafkaPublishTarget resolve(Object event) {
    return switch (event) {
      // member
      case MemberSignupEvent e -> resolveMemberEvent(e.memberId());
      case MemberBasicInfoUpdatedEvent e -> resolveMemberEvent(e.memberId());
      case MemberProfileCreatedEvent e -> resolveMemberEvent(e.memberId());
      case MemberProfileUpdatedEvent e -> resolveMemberEvent(e.memberId());
      case MemberDeliveryAddressAddedEvent e -> resolveMemberEvent(e.memberId());
      case MemberDeliveryAddressUpdatedEvent e -> resolveMemberEvent(e.memberId());
      case MemberDeliveryAddressDeletedEvent e -> resolveMemberEvent(e.memberId());
      case MemberDeliveryAddressSetAsDefaultEvent e -> resolveMemberEvent(e.memberId());
      case SellerRegisteredEvent e ->
          resolveSellerRegisteredEvent(e.memberId(), e.memberSellerId());

      // payment
      case PaymentMemberCreatedEvent e -> resolvePaymentMemberEvent(e.traceId(), e.memberId());
      case PaymentFailedEvent e -> resolvePaymentEvent(e.traceId(), e.memberId(), e.orderNo());
      case PaymentSuccessEvent e ->
          resolvePaymentEvent(e.traceId(), e.payment().memberId(), e.payment().orderNo());
      case PaymentRefundSuccessEvent e ->
          resolvePaymentEvent(e.traceId(), e.payment().memberId(), e.payment().orderNo());
      case PaymentFinalFailureEvent e ->
          resolvePaymentEvent(e.traceId(), e.payment().memberId(), e.payment().orderNo());

      // product
      case ProductCreatedEvent e -> resolveProduct(e.productDto().getId());
      case ProductUpdatedEvent e -> resolveProduct(e.productDto().getId());
      case ProductOrderAvailabilityChangedEvent e ->
          resolveProduct(e.productOrderAvailableDto().productId());

      // order
      case OrderPurchaseConfirmedEvent e -> resolveOrder(e.orderDto().getOrderId());
      case OrderCancelRequestEvent e -> resolveOrder(e.orderDto().getOrderId());
      case RefundRequestedEvent e -> resolveOrder(e.orderDto().getOrderId());

      // settlement
      case SettlementCompletedPayoutEvent e -> resolveSettlement(e.batchId());

      // default
      default ->
          KafkaPublishTarget.of(
              UUID.randomUUID().toString(), "unexpected-events-topic", "Unknown", "unexpected-key");
    };
  }

  private KafkaPublishTarget resolveMemberEvent(Long memberId) {
    return KafkaPublishTarget.of(
        UUID.randomUUID().toString(),
        MEMBER_EVENTS_TOPIC,
        "Member",
        "member-%d".formatted(memberId));
  }

  private KafkaPublishTarget resolveSellerRegisteredEvent(Long memberId, Long memberSellerId) {
    return KafkaPublishTarget.of(
        UUID.randomUUID().toString(),
        MEMBER_EVENTS_TOPIC,
        "Member",
        "member-%d-seller-%d".formatted(memberId, memberSellerId));
  }

  private KafkaPublishTarget resolvePaymentMemberEvent(String traceId, Long memberId) {
    return KafkaPublishTarget.of(
        traceId, PAYMENT_EVENTS_TOPIC, "PaymentMember", "payment-member-%d".formatted(memberId));
  }

  private KafkaPublishTarget resolvePaymentEvent(String traceId, Long memberId, String orderNo) {
    return KafkaPublishTarget.of(
        traceId, PAYMENT_EVENTS_TOPIC, "Payment", "payment-%d-%s".formatted(memberId, orderNo));
  }

  private KafkaPublishTarget resolveProduct(Long productId) {
    return KafkaPublishTarget.of(
        UUID.randomUUID().toString(),
        PRODUCT_EVENTS_TOPIC,
        "Product",
        "product-%d".formatted(productId));
  }

  private KafkaPublishTarget resolveOrder(Long orderId) {
    return KafkaPublishTarget.of(
        UUID.randomUUID().toString(), ORDER_EVENTS_TOPIC, "Order", "order-%d".formatted(orderId));
  }

  private KafkaPublishTarget resolveSettlement(String batchId) {
    return KafkaPublishTarget.of(
        UUID.randomUUID().toString(),
        SETTLEMENT_EVENTS_TOPIC,
        "Settlement",
        "settlement-%s".formatted(batchId));
  }
}
