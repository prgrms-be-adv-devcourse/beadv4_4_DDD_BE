#!/bin/bash

# Debezium Outbox Connector 등록 스크립트

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
if [ -f "$ENV_FILE" ]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

DEBEZIUM_HOST=${DEBEZIUM_HOST:-localhost:8093}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-}

if [ -z "MYSQL_ROOT_PASSWORD" ]; then
  echo "Error: .env 또는 환경변수에 MYSQL_ROOT_PASSWORD 설정하세요"
  exit 1
fi

curl -X POST "http://${DEBEZIUM_HOST}/connectors" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"outbox-connector\",
    \"config\": {
      \"connector.class\": \"io.debezium.connector.mysql.MySqlConnector\",
      \"database.hostname\": \"mysql\",
      \"database.port\": \"3306\",
      \"database.user\": \"root\",
      \"database.password\": \"${MYSQL_ROOT_PASSWORD}\",
      \"database.server.id\": \"1\",
      \"database.include.list\": \"modeunsa\",
      \"table.include.list\": \".*outbox_event\",
      \"topic.prefix\": \"outbox\",
      \"schema.history.internal.kafka.bootstrap.servers\": \"kafka:9092\",
      \"schema.history.internal.kafka.topic\": \"schema-changes.outbox\",
      \"transforms\": \"outbox,envelope\",
      \"transforms.outbox.type\": \"io.debezium.transforms.outbox.EventRouter\",
      \"transforms.outbox.table.field.event.id\": \"id\",
      \"transforms.outbox.table.field.event.key\": \"aggregate_id\",
      \"transforms.outbox.table.field.event.payload\": \"payload\",
      \"transforms.outbox.table.field.event.type\": \"event_type\",
      \"transforms.outbox.route.by.field\": \"topic\",
      \"transforms.outbox.route.topic.replacement\": \"\${routedByValue}\",
      \"transforms.outbox.table.fields.additional.placement\": \"event_id:header:eventId,trace_id:header:traceId,event_type:header:eventType,topic:header:topic\",
      \"transforms.envelope.type\": \"com.modeunsa.debezium.smt.DomainEnvelopeWrapper\$Value\",
      \"key.converter\": \"org.apache.kafka.connect.storage.StringConverter\",
      \"value.converter\": \"org.apache.kafka.connect.json.JsonConverter\",
      \"value.converter.schemas.enable\": \"false\"
    }
  }"