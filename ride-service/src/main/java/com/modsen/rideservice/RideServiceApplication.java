package com.modsen.rideservice;

import com.modsen.rideservice.config.kafka.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
@EnableEurekaClient
@EnableFeignClients
public class RideServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RideServiceApplication.class, args);
  }
}
