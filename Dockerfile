FROM maven:3.3.2-jdk-11 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build /target/viet-chat-api-0.0.1.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080