package com.modeunsa.global.eventpublisher.topic;

public record KafkaPublishTarget(
    String traceId, String topic, String aggregateType, String aggregateId) {

  public String kafkaKey() {
    return aggregateId;
  }

  public static KafkaPublishTarget of(
      String traceId, String topic, String aggregateType, String aggregateId) {
    return new KafkaPublishTarget(traceId, topic, aggregateType, aggregateId);
  }
}
