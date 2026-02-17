package com.modeunsa.boundedcontext.payment.in.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Debezium/Connect가 value를 {"schema":..., "payload": {...}} 형태로 보내서 공통 Jackson 역직렬화가 실패한다.
 * payment-events만 payload를 꺼내 DomainEventEnvelope로 역직렬화하는 전용 팩토리를 사용한다.
 */
@Configuration
public class PaymentEventsKafkaConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
  private String bootstrapServers;

  @Bean("paymentEventsListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, Object> paymentEventsListenerContainerFactory(
      @Qualifier("kafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
    factory.setConsumerFactory(paymentEventsConsumerFactory());
    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    var recoverer =
        new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) ->
                new TopicPartition(record.topic() + ".DLT", record.partition()));
    var backoff = new FixedBackOff(1000L, 3);
    var errorHandler = new DefaultErrorHandler(recoverer, backoff);
    errorHandler.addNotRetryableExceptions(DeserializationException.class);
    factory.setCommonErrorHandler(errorHandler);

    return factory;
  }

  private ConsumerFactory<String, Object> paymentEventsConsumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
    props.put(
        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
        DebeziumEnvelopeDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
    props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
    return new DefaultKafkaConsumerFactory<>(props);
  }
}
