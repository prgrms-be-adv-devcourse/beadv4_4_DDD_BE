package com.modeunsa.boundedcontext.payment.app.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxStore;
import com.modeunsa.global.eventpublisher.topic.KafkaTopics;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.global.kafka.outbox.OutboxEventMetadata;
import com.modeunsa.global.kafka.outbox.OutboxPublisher;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import com.modeunsa.shared.payment.event.PaymentFinalFailureEvent;
import com.modeunsa.shared.payment.event.PaymentMemberCreatedEvent;
import com.modeunsa.shared.payment.event.PaymentRefundSuccessEvent;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxPublisher implements OutboxPublisher {

  private final PaymentOutboxStore paymentOutboxStore;
  private final JsonConverter jsonConverter;

  @Override
  public void saveToOutbox(Object event) {

    OutboxEventMetadata metadata = resolveMetadata(event);
    if (metadata == null) {
      log.error("Unsupported event type: {}", event.getClass());
      return;
    }

    String payload = jsonConverter.serialize(event);
    PaymentOutboxEvent outboxEvent =
        PaymentOutboxEvent.create(
            metadata.aggregateType(),
            metadata.aggregateId(),
            String.valueOf(event.getClass()),
            metadata.topic(),
            payload);

    paymentOutboxStore.store(outboxEvent);
  }

  private OutboxEventMetadata resolveMetadata(Object event) {
    return switch (event) {
      case PaymentMemberCreatedEvent e ->
          new OutboxEventMetadata(
              "Payment", e.memberId().toString(), KafkaTopics.PAYMENT_MEMBER_CREATED);
      case PaymentSuccessEvent e ->
          new OutboxEventMetadata(
              "Payment",
              String.format("%s-%s", e.payment().memberId(), e.payment().orderNo()),
              KafkaTopics.PAYMENT_SUCCESS);
      case PaymentFailedEvent e ->
          new OutboxEventMetadata(
              "Payment",
              String.format("%s-%s", e.memberId(), e.orderNo()),
              KafkaTopics.PAYMENT_FAILED);
      case PaymentFinalFailureEvent e ->
          new OutboxEventMetadata(
              "Payment",
              String.format("%s-%s", e.payment().memberId(), e.payment().orderNo()),
              KafkaTopics.PAYMENT_FINAL_FAILED);
      case PaymentRefundSuccessEvent e ->
          new OutboxEventMetadata(
              "Payment",
              String.format("%s-%s", e.payment().memberId(), e.payment().orderNo()),
              KafkaTopics.PAYMENT_REFUND_SUCCESS);
      default -> null;
    };
  }
}
