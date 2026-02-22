package com.modeunsa.global.config;

import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.eventpublisher.KafkaDomainEventPublisher;
import com.modeunsa.global.eventpublisher.OutboxDomainEventPublisher;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.eventpublisher.topic.KafkaResolver;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.global.kafka.outbox.OutboxPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventConfig {

  @Bean
  @ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "spring")
  public EventPublisher springEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    return new SpringDomainEventPublisher(applicationEventPublisher);
  }

  @Bean
  @ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "kafka")
  public EventPublisher kafkaEventPublisher(
      KafkaTemplate<String, Object> kafkaTemplate,
      KafkaResolver kafkaResolver,
      JsonConverter jsonConverter) {
    return new KafkaDomainEventPublisher(kafkaTemplate, kafkaResolver, jsonConverter);
  }

  @Bean
  @ConditionalOnProperty(name = "app.event-publisher.type", havingValue = "outbox")
  public EventPublisher outboxEventPublisher(OutboxPublisher outboxPublisher) {
    return new OutboxDomainEventPublisher(outboxPublisher);
  }
}
