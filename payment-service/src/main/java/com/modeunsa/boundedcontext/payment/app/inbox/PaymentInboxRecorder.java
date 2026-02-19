package com.modeunsa.boundedcontext.payment.app.inbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentInboxEvent;
import com.modeunsa.boundedcontext.payment.out.PaymentInboxReader;
import com.modeunsa.boundedcontext.payment.out.PaymentInboxStore;
import com.modeunsa.global.kafka.inbox.InboxRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentInboxRecorder implements InboxRecorder {

  private final PaymentInboxReader paymentInboxReader;
  private final PaymentInboxStore paymentInboxStore;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public boolean tryRecord(String eventId, String topic, String payload, String traceId) {
    if (paymentInboxReader.existsByEventId(eventId)) {
      return true;
    }

    PaymentInboxEvent inboxEvent = PaymentInboxEvent.create(eventId, topic, payload, traceId);
    paymentInboxStore.store(inboxEvent);
    return false;
  }
}
