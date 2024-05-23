FROM maven:3.8.3-jdk-17 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build /target/viet-chat-api-0.0.1.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080