FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# non-root 사용자 생성
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown -R appuser:appgroup /app

# non-root 사용자로 전환
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]