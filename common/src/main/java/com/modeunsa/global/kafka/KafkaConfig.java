package com.modeunsa.global.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
  private String bootstrapServers;

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
    // 모든 레플리카 복제되었는지 확인 후에 ACK 를 받도록 설정하여 데이터 손실 방지
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");
    configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
    // 전송 타임아웃
    configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
    // 중복 전송 방지
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    // 최대 5개의 요청이 동시에 처리될 수 있도록 설정
    // 한 번에 많이 보내면 성능은 향상되지만 순서 보장 및 안정성에서 문제가 발생할 수 있기 때문에 낮은 값 선호
    configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
    // 메시지를 배치로 묶어서 전송하는 지연 시간 설정 (ms 단위)
    // 이 값이 크면 메시지를 더 많이 배치로 묶어서 자원을 효율적으로 쓰지만 지연이 발생할 수 있음
    configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
    // kafka header 에 타입 정보를 포함하여 consumer 가 역직렬화 할 수 있도록 도움
    configProps.put(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, true);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ConsumerFactory<String, Object> consumerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // 역직렬화 실패 시 쓰레드가 바로 죽지 않고 에러 정보를 헤더/예외로 전달해서 DLQ 처리나 로깅을 진행하게 함
    // ErrorHandlingDeserializer 로 래핑
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    configProps.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    // key 는 String, value 는 JSON 역직렬화 하도록 설정
    configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
    configProps.put(
        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);
    // 역직렬화 시 신뢰할 수 있는 패키지 설정
    // TODO: 패키지 범위를 좁혀서 보안 강화
    configProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.modeunsa.*");
    // 역직렬화 시 타입 정보를 kafka header 에서 읽어서 처리하도록 설정
    configProps.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, true);
    // 처음 컨슈머 그룹 붙었을 시 offset 이 없으면 최신 메시지부터 읽음
    configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    // kafka 가 자동커밋 하지 않고 리스너에서 수동으로 커밋하게 함
    // 리스너에서 처리 성공 후에서만 커밋을 해야 함
    configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    // 최대 폴링 레코드 수
    configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
    // 최대 폴링 대기시간
    configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
    // 300초 동안 폴링이 없으면 컨슈머가 죽었다고 판단해서 리밸런싱이 일어남
    configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
    return new DefaultKafkaConsumerFactory<>(configProps);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
    factory.setConsumerFactory(consumerFactory());
    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    // DLQ Recover
    // 기존 topic 이름 뒤에 .DLT 를 붙여서 DLT 토픽으로 메시지를 보냄
    var recoverer =
        new DeadLetterPublishingRecoverer(
            kafkaTemplate(),
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));

    // 재시도 정책 : 1초 간격 3번 재시도
    var backoff = new FixedBackOff(1000L, 3);
    var errorHandler = new DefaultErrorHandler(recoverer, backoff);

    // 역직렬화 예외의 경우에는 재시도 하지 않음
    errorHandler.addNotRetryableExceptions(DeserializationException.class);

    factory.setCommonErrorHandler(errorHandler);

    return factory;
  }
}
