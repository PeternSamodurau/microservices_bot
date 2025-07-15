FROM eclipse-temurin:17-jre-focal

WORKDIR /app

COPY build/libs/dispatcher-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]

