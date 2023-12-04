package com.modsen.rideservice;

import com.modsen.rideservice.config.kafka.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class RideServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RideServiceApplication.class, args);
  }
}
