FROM openjdk:17-jdk-alpine
LABEL maintainer="Steve Limb"

# Expose 8080 so that we can get liveness and health checks.
EXPOSE 8080
COPY target/boot-0.0.1-SNAPSHOT.jar boot-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/boot-0.0.1-SNAPSHOT.jar"]