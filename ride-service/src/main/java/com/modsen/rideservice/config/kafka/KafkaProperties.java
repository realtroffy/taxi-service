package com.modsen.rideservice.config.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "spring.kafka")
@ConfigurationPropertiesScan
@Getter
@Setter
public class KafkaProperties {

  private String bootstrapAddress;
  private String topicAvailableDriver;
  private String topicNotFoundDriver;
  private String consumersGroupIdAvailableDriver;
  private String consumersGroupIdNotFoundAvailableDriver;
}
