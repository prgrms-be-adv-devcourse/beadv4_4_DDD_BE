# SMT JAR (Java 11)을 MySQL 커넥터와 같은 플러그인 디렉터리에 넣어서 같은 classloader에서 로드되게 함.
# 빌드 전: ./gradlew :kafka-connector:jar && cp kafka-connector/build/libs/debezium-envelope-wrapper-1.0.0.jar infra/debezium/plugins/
# 빌드: docker build -f infra/debezium/Dockerfile -t debezium-connect-with-smt infra/debezium
FROM debezium/connect:2.5

COPY plugins/debezium-envelope-wrapper-1.0.0.jar /tmp/smt.jar
# MySQL 커넥터와 같은 플러그인 디렉터리에 SMT JAR 추가 (KAFKA_HOME=/kafka 기준)
# tar 추출 시 디렉터리명이 debezium-connector-mysql 또는 debezium-connector-mysql-* 형태
RUN MYSQL_PLUGIN=$(find /kafka/connect -maxdepth 1 -type d -name '*mysql*' 2>/dev/null | head -1) && \
    if [ -n "$MYSQL_PLUGIN" ]; then \
      cp /tmp/smt.jar "$MYSQL_PLUGIN/"; \
    else \
      echo "=== /kafka/connect contents ===" && ls -la /kafka/connect && \
      echo "=== first level dirs ===" && ls -la /kafka/connect/ && \
      exit 1; \
    fi
