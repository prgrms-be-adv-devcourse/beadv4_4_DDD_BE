package com.modeunsa.boundedcontext.payment.in.listener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.kafka.inbox.DuplicateInboxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.event-consumer.type", havingValue = "kafka")
@RequiredArgsConstructor
public class PaymentKafkaEventListener {

  private final PaymentEventHandler paymentEventHandler;

  @KafkaListener(topics = "member-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
    try {
      paymentEventHandler.handle(envelope);
      ackAfterCommit(ack);
    } catch (DuplicateInboxException e) {
      ack.acknowledge();
    }
  }

  @KafkaListener(topics = "payment-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePaymentEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
    try {
      paymentEventHandler.handle(envelope);
      ackAfterCommit(ack);
    } catch (DuplicateInboxException e) {
      ack.acknowledge();
    }
  }

  @KafkaListener(topics = "order-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
    try {
      paymentEventHandler.handle(envelope);
      ackAfterCommit(ack);
    } catch (DuplicateInboxException e) {
      ack.acknowledge();
    }
  }

  @KafkaListener(topics = "settlement-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePayoutEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
    try {
      paymentEventHandler.handle(envelope);
      ackAfterCommit(ack);
    } catch (DuplicateInboxException e) {
      ack.acknowledge();
    }
  }

  private void ackAfterCommit(Acknowledgment ack) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            ack.acknowledge();
          }
        });
  }
}
