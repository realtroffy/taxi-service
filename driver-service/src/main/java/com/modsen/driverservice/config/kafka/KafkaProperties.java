package com.modsen.driverservice.config.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "spring.kafka")
@ConfigurationPropertiesScan
@Getter
@Setter
public class KafkaProperties {

  private String bootstrapServers;
  private String idConfig;
  private String topicAvailableDriver;
  private String topicNotFoundDriver;
  private String topicOrderNewRide;
}
