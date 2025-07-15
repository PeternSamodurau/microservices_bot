FROM eclipse-temurin:17-jdk
WORKDIR /app

# Копируем все jar-файлы микросервисов
COPY rest-service/build/libs/rest-service-0.0.1-SNAPSHOT.jar rest.jar
COPY node/build/libs/node-0.0.1-SNAPSHOT.jar node.jar
COPY mail-service/build/libs/mail-service-0.0.1-SNAPSHOT.jar mail.jar
COPY dispatcher/build/libs/dispatcher-0.0.1-SNAPSHOT.jar dispatcher.jar

# Поддержка переменных среды, передаваемых CleverCloud
ENV JAVA_OPTS="-XX:+UseContainerSupport"

# Запуск всех сервисов параллельно внутри одного контейнера
CMD ["sh", "-c", \
  "java $JAVA_OPTS -jar rest.jar & \
   java $JAVA_OPTS -jar node.jar & \
   java $JAVA_OPTS -jar mail.jar & \
   java $JAVA_OPTS -jar dispatcher.jar && \
   wait"]
