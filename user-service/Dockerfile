FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*.jar user-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "user-service.jar"]
