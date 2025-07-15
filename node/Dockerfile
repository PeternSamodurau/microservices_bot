FROM eclipse-temurin:17-jre-focal

WORKDIR /app

COPY build/libs/node-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
