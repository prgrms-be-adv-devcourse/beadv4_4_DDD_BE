package com.modeunsa.global.eventpublisher.topic;

import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaResolver {

  private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

  public String resolveTopic(Object event) {
    if (event instanceof PaymentMemberCreatedEvent) {
      return PAYMENT_EVENTS_TOPIC;
    }

    return "unexpected-events-topic";
  }

  public String resolveKey(Object event) {
    if (event instanceof PaymentMemberCreatedEvent e) {
      return "member-%d".formatted(e.memberId());
    }

    return "unexpected-key";
  }
}
