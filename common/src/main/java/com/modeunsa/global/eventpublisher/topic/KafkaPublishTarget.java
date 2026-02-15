package com.modeunsa.global.eventpublisher.topic;

public record KafkaPublishTarget(String topic, String aggregateType, String aggregateId) {

  public String kafkaKey() {
    return aggregateId;
  }

  public static KafkaPublishTarget of(String topic, String aggregateType, String aggregateId) {
    return new KafkaPublishTarget(topic, aggregateType, aggregateId);
  }
}
