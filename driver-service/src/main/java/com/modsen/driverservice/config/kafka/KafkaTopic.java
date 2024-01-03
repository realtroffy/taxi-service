package com.modsen.driverservice.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaTopic {

  private final KafkaProperties kafkaProperties;

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic createTopicOrderNewRide() {
    return TopicBuilder.name(kafkaProperties.getTopicOrderNewRide()).build();
  }

  @Bean
  public NewTopic createTopicAvailableDriver() {
    return TopicBuilder.name(kafkaProperties.getTopicAvailableDriver()).build();
  }

  @Bean
  public NewTopic createTopicNotFoundDriver() {
    return TopicBuilder.name(kafkaProperties.getTopicNotFoundDriver()).build();
  }
}
