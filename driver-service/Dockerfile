FROM openjdk:11

ARG JAR_FILE=target/driver-service.jar

WORKDIR /opt/taxi-service

COPY ${JAR_FILE} driver-service.jar

ENTRYPOINT ["java","-jar","driver-service.jar"]