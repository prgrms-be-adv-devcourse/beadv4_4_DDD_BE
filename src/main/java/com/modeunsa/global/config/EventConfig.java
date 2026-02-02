package com.modeunsa.global.config;

import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.eventpublisher.KafkaDomainEventPublisher;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.eventpublisher.topic.KafkaResolver;
import com.modeunsa.global.json.JsonConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventConfig {

  @Bean
  public EventPublisher eventPublisher(
      @Value("${app.event-publisher.type}") String type,
      ApplicationEventPublisher applicationEventPublisher,
      KafkaTemplate<String, Object> kafkaTemplate,
      KafkaResolver topicResolver,
      JsonConverter jsonConverter) {

    return "kafka".equalsIgnoreCase(type)
        ? new KafkaDomainEventPublisher(kafkaTemplate, topicResolver, jsonConverter)
        : new SpringDomainEventPublisher(applicationEventPublisher);
  }
}
