package com.modeunsa.global.config;

import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.eventpublisher.KafkaDomainEventPublisher;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.eventpublisher.topic.KafkaResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class EventConfig {

  @Bean
  public EventPublisher eventPublisher(
      @Value("${app.event-publisher.type}") String type,
      ApplicationEventPublisher applicationEventPublisher,
      KafkaTemplate<String, Object> kafkaTemplate,
      KafkaResolver topicResolver,
      ObjectMapper objectMapper) {

    return "kafka".equalsIgnoreCase(type)
        ? new KafkaDomainEventPublisher(kafkaTemplate, topicResolver, objectMapper)
        : new SpringDomainEventPublisher(applicationEventPublisher);
  }
}
