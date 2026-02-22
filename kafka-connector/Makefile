# kafka-connector 모듈 기준 (이 디렉터리에서 make 실행)
# Debezium Outbox + SMT (DomainEnvelopWrapper) 빌드·배포

ROOT := ..
SMT_JAR_NAME := debezium-envelope-wrapper-1.0.0.jar
KAFKA_CONNECTOR_JAR := build/libs/$(SMT_JAR_NAME)
PLUGINS_DIR := plugins
SCRIPTS_DIR := scripts
DEBEZIUM_IMAGE := debezium-connect-with-smt
DEBEZIUM_DOCKERFILE := Dockerfile
DEBEZIUM_CONTEXT := .

.PHONY: jar
jar:
	$(ROOT)/gradlew :kafka-connector:jar --no-daemon -q
	@echo "Built $(KAFKA_CONNECTOR_JAR)"

.PHONY: install
install: jar
	@mkdir -p $(PLUGINS_DIR)
	cp $(KAFKA_CONNECTOR_JAR) $(PLUGINS_DIR)/
	@echo "Installed $(SMT_JAR_NAME) to $(PLUGINS_DIR)/"

.PHONY: docker-build
docker-build: install
	docker build -f $(DEBEZIUM_DOCKERFILE) -t $(DEBEZIUM_IMAGE) $(DEBEZIUM_CONTEXT)
	@echo "Image $(DEBEZIUM_IMAGE) built"

.PHONY: connector-register
connector-register:
	./$(SCRIPTS_DIR)/register-debezium-connector.sh

.PHONY: connector-delete
connector-delete:
	./$(SCRIPTS_DIR)/delete-debezium-connector.sh

.PHONY: all
all: docker-build
	@echo "Run: docker compose up -d debezium && make connector-register"
