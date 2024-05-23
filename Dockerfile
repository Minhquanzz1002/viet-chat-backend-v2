FROM maven:3.3.2-jdk-11 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

FROM openjdk:17-jdk-slim
LABEL authors="Minh Quan"
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080