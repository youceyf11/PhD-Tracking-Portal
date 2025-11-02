FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*.jar inscription-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "user-service.jar"]
