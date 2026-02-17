package com.modeunsa.debezium.smt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Header;
import org.apache.kafka.connect.transforms.Transformation;

/**
 * Debezium Outbox EventRouter 출력을 다음 형식으로 맞춘다.
 *
 * <ul>
 *   <li>key: 문자열 "payment-member-{member_id}" (keySchema=null, key=String) →
 *       key.converter=StringConverter
 *   <li>value: envelope Map (valueSchema=null) → value.converter=JsonConverter,
 *       schemas.enable=false 로 JSON 한 겹만 출력
 *   <li>header: __TypeId__ = com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope 만
 * </ul>
 *
 * Java 11 호환 (Debezium Connect 컨테이너 런타임).
 */
public class DomainEnvelopeWrapper<R extends ConnectRecord<R>> implements Transformation<R> {

  private static final String EVENT_ID_HEADER = "eventId";
  private static final String EVENT_TYPE_HEADER = "eventType";
  private static final String TOPIC_HEADER = "topic";
  private static final String TRACE_ID_HEADER = "traceId";
  private static final String FIELD_OCCURRED_AT = "occurredAt";
  private static final String FIELD_PAYLOAD = "payload";

  private static final String TYPE_ID_HEADER = "__TypeId__";
  private static final String TYPE_ID_VALUE =
      "com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope";

  @Override
  public R apply(R record) {
    Object value = record.value();
    if (value == null) {
      return record;
    }

    String payload = extractPayload(value);
    String eventId = nullToEmpty(getHeaderString(record, EVENT_ID_HEADER));
    if (eventId.isEmpty()) {
      eventId = UUID.randomUUID().toString();
    }
    String eventType = nullToEmpty(getHeaderString(record, EVENT_TYPE_HEADER));
    String topic = nullToEmpty(getHeaderString(record, TOPIC_HEADER));
    String traceId = nullToEmpty(getHeaderString(record, TRACE_ID_HEADER));

    String occurredAt =
        record.timestamp() != null
            ? Instant.ofEpochMilli(record.timestamp()).toString()
            : Instant.now().toString();

    Map<String, Object> envelopeMap = new HashMap<>();
    envelopeMap.put(EVENT_ID_HEADER, eventId);
    envelopeMap.put(EVENT_TYPE_HEADER, eventType);
    envelopeMap.put(FIELD_OCCURRED_AT, occurredAt);
    envelopeMap.put(TOPIC_HEADER, topic);
    envelopeMap.put(FIELD_PAYLOAD, payload != null ? payload : "");
    envelopeMap.put(TRACE_ID_HEADER, traceId);

    String keyString = extractKeyString(record);

    ConnectHeaders headers = new ConnectHeaders();
    headers.add(TYPE_ID_HEADER, TYPE_ID_VALUE, Schema.STRING_SCHEMA);

    return record.newRecord(
        record.topic(),
        record.kafkaPartition(),
        null,
        keyString,
        null,
        envelopeMap,
        record.timestamp(),
        headers);
  }

  private String extractKeyString(R record) {
    Object key = record.key();
    if (key == null) {
      return "unknown";
    }

    if (key instanceof String) {
      return (String) key;
    }

    return "unknown";
  }

  private static String nullToEmpty(String s) {
    return s != null ? s : "";
  }

  private String extractPayload(Object value) {
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof byte[]) {
      return new String((byte[]) value, StandardCharsets.UTF_8);
    }
    if (value instanceof Struct) {
      Struct struct = (Struct) value;
      if (struct.schema().field(FIELD_PAYLOAD) != null) {
        Object p = struct.get(FIELD_PAYLOAD);
        return p != null ? p.toString() : "";
      }
    }
    return value.toString();
  }

  private String getHeaderString(R record, String name) {
    Header h = record.headers().lastWithName(name);
    if (h == null || h.value() == null) {
      return "";
    }
    Object v = h.value();
    if (v instanceof byte[]) {
      return new String((byte[]) v, StandardCharsets.UTF_8);
    }
    return v.toString();
  }

  @Override
  public ConfigDef config() {
    return new ConfigDef();
  }

  @Override
  public void configure(Map<String, ?> configs) {
    // no config
  }

  @Override
  public void close() {}

  /** Connector 설정에서 Value 전용 SMT로 쓸 때: DomainEnvelopWrapper$Value */
  public static class Value<R extends ConnectRecord<R>> extends DomainEnvelopeWrapper<R> {}
}
