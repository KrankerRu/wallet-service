FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/wallet-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]