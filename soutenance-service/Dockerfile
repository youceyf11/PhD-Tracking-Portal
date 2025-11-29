FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/soutenance-service-0.0.1-SNAPSHOT.jar soutenance-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "soutenance-service.jar"]
