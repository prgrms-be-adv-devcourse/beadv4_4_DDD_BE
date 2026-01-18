FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/modeunsa_be-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]