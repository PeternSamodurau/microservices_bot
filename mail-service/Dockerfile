FROM eclipse-temurin:17-jre-focal

WORKDIR /app

COPY build/libs/mail-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
