package com.modeunsa.global.eventpublisher;

import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.eventpublisher.topic.KafkaPublishTarget;
import com.modeunsa.global.eventpublisher.topic.KafkaResolver;
import com.modeunsa.global.json.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements EventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final KafkaResolver kafkaResolver;
  private final JsonConverter jsonConverter;

  @Override
  public void publish(Object event) {
    KafkaPublishTarget target = kafkaResolver.resolve(event);
    String payload = jsonConverter.serialize(event);
    DomainEventEnvelope envelope = DomainEventEnvelope.of(event, target.topic(), payload);

    var message =
        MessageBuilder.withPayload(envelope)
            .setHeader(KafkaHeaders.TOPIC, target.topic())
            .setHeader(KafkaHeaders.KEY, target.kafkaKey())
            .setHeader("eventType", envelope.eventType())
            .setHeader("eventId", envelope.eventId())
            .build();

    kafkaTemplate
        .send(message)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error(
                    "Failed to publish event to Kafka topic: {}, eventType={}, eventId={}",
                    target.topic(),
                    envelope.eventType(),
                    envelope.eventId(),
                    ex);
              }
            });
  }
}
