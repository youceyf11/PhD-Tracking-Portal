FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/inscription-service-0.0.1-SNAPSHOT.jar inscription-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "inscription-service.jar"]
