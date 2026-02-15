package com.modeunsa.boundedcontext.payment.app.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxStore;
import com.modeunsa.global.eventpublisher.topic.KafkaPublishTarget;
import com.modeunsa.global.eventpublisher.topic.KafkaResolver;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.global.kafka.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxPublisher implements OutboxPublisher {

  private final PaymentOutboxStore paymentOutboxStore;
  private final JsonConverter jsonConverter;
  private final KafkaResolver kafkaResolver;

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void saveToOutbox(Object event) {

    KafkaPublishTarget target = kafkaResolver.resolve(event);
    if (target == null) {
      log.error("Unsupported event type: {}", event.getClass());
      return;
    }

    String payload = jsonConverter.serialize(event);
    PaymentOutboxEvent outboxEvent =
        PaymentOutboxEvent.create(
            target.aggregateType(),
            target.aggregateId(),
            event.getClass().getSimpleName(),
            target.topic(),
            payload,
            target.traceId());

    paymentOutboxStore.store(outboxEvent);
  }
}
