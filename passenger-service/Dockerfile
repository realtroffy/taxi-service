FROM openjdk:11

ARG JAR_FILE=target/passenger-service.jar

WORKDIR /opt/taxi-service

COPY ${JAR_FILE} passenger-service.jar

ENTRYPOINT ["java","-jar","passenger-service.jar"]