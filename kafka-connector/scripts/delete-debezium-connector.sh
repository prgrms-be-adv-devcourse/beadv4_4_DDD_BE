#!/bin/bash

DEBEZIUM_HOST=${DEBEZIUM_HOST:-localhost:8093}
MYSQL_PASSWORD=${MYSQL_ROOT_PASSWORD:-HJk9Uyw3lqzpBLgynevsQw==}
CONNECTOR_NAME="outbox-connector"

if [ -z "$MYSQL_PASSWORD" ]; then
  echo "Error: MYSQL_ROOT_PASSWORD or MYSQL_PASSWORD environment variable is not set"
  exit 1
fi

echo "Deleting connector ${CONNECTOR_NAME}..."
curl -s -X DELETE "http://${DEBEZIUM_HOST}/connectors/${CONNECTOR_NAME}"
echo ""
