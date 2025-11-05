FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/config-server-0.0.1-SNAPSHOT.jar config-server.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "config-server.jar"]