FROM openjdk:11

ARG JAR_FILE=target/ride-service.jar

WORKDIR /opt/taxi-service

COPY ${JAR_FILE} ride-service.jar

ENTRYPOINT ["java","-jar","ride-service.jar"]