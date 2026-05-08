FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_PATH
ARG SERVER_PORT

COPY ${JAR_PATH} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE ${SERVER_PORT}
