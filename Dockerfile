FROM openjdk:17-jdk-slim
LABEL authors="Minh Quan"
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080