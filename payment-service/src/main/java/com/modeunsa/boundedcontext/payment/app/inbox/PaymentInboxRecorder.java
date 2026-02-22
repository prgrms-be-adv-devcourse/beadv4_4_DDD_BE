package com.modeunsa.boundedcontext.payment.app.inbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentInboxEvent;
import com.modeunsa.boundedcontext.payment.out.PaymentInboxReader;
import com.modeunsa.boundedcontext.payment.out.PaymentInboxStore;
import com.modeunsa.global.kafka.inbox.InboxRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    try {
      paymentInboxStore.store(inboxEvent);
      return false;
    } catch (DataIntegrityViolationException e) {
      log.warn("Inbox event already exists for eventId: {}, topic: {}", eventId, topic);
      return true;
    }
  }
}
