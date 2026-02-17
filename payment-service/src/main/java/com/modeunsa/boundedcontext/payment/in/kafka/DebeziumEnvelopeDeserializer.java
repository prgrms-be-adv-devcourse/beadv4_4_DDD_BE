package com.modeunsa.boundedcontext.payment.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import java.io.IOException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Kafka Connect/Debezium이 value를 {"schema":..., "payload": {...}} 형태로 보낼 때,
 * payload만 꺼내서 DomainEventEnvelope로 역직렬화한다.
 * 리스너는 동일하게 DomainEventEnvelope를 받는다.
 */
public class DebeziumEnvelopeDeserializer implements Deserializer<DomainEventEnvelope> {

  private static final String PAYLOAD_KEY = "payload";

  private final ObjectMapper objectMapper;

  public DebeziumEnvelopeDeserializer() {
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  @Override
  public DomainEventEnvelope deserialize(String topic, byte[] data) {
    if (data == null || data.length == 0) {
      return null;
    }
    try {
      JsonNode root = objectMapper.readTree(data);
      if (root == null || !root.isObject()) {
        return objectMapper.readValue(data, DomainEventEnvelope.class);
      }
      if (root.has(PAYLOAD_KEY) && root.get(PAYLOAD_KEY).isObject()) {
        JsonNode payloadNode = root.get(PAYLOAD_KEY);
        return objectMapper.treeToValue(payloadNode, DomainEventEnvelope.class);
      }
      return objectMapper.treeToValue(root, DomainEventEnvelope.class);
    } catch (IOException e) {
      throw new org.springframework.kafka.support.serializer.DeserializationException(
          "Failed to deserialize to DomainEventEnvelope", data, false, e);
    }
  }
}
