#!/bin/bash

DEBEZIUM_HOST=${DEBEZIUM_HOST:-localhost:8093}
CONNECTOR_NAME="outbox-connector"

echo "Deleting connector ${CONNECTOR_NAME}..."
curl -s -X DELETE "http://${DEBEZIUM_HOST}/connectors/${CONNECTOR_NAME}"
echo ""
