package com.modeunsa.global.eventpublisher.topic;

import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.order.event.RefundRequestedEvent;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaResolver {

  private static final String MEMBER_EVENTS_TOPIC = "member-events";
  private static final String PAYMENT_EVENTS_TOPIC = "payment-events";
  private static final String ORDER_EVENTS_TOPIC = "order-events";
  private static final String SETTLEMENT_EVENTS_TOPIC = "settlement-events";

  public String resolveTopic(Object event) {

    if (event instanceof MemberSignupEvent) {
      return MEMBER_EVENTS_TOPIC;
    }
    if (event instanceof PaymentMemberCreatedEvent) {
      return PAYMENT_EVENTS_TOPIC;
    }
    if (event instanceof PaymentFailedEvent) {
      return PAYMENT_EVENTS_TOPIC;
    }
    if (event instanceof RefundRequestedEvent) {
      return ORDER_EVENTS_TOPIC;
    }
    if (event instanceof SettlementCompletedPayoutEvent) {
      return SETTLEMENT_EVENTS_TOPIC;
    }

    return "unexpected-events-topic";
  }

  // key 는 같은 topic 안에서 동일한 key 라면 같은 파티션에서 순차적으로 메시지가 처리된다.
  public String resolveKey(Object event) {
    if (event instanceof MemberSignupEvent e) {
      return "member-%d".formatted(e.memberId());
    }
    if (event instanceof PaymentMemberCreatedEvent e) {
      return "payment-member-%d".formatted(e.memberId());
    }
    if (event instanceof PaymentFailedEvent e) {
      return "payment-%d-%s".formatted(e.memberId(), e.orderNo());
    }
    if (event instanceof RefundRequestedEvent e) {
      return "order-%s".formatted(e.orderDto().getOrderNo());
    }
    if (event instanceof SettlementCompletedPayoutEvent e) {
      return "settlement";
    }

    return "unexpected-key";
  }
}
