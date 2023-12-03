package com.modsen.rideservice.config.kafka;

import com.modsen.rideservice.dto.DriverRideDto;
import com.modsen.rideservice.dto.RideSearchDto;
import com.modsen.rideservice.service.RideService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

  private final RideService rideService;

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;

  @Value(value = "${spring.kafka.topic.available.driver}")
  private String availableDriverTopic;

  @Value(value = "${spring.kafka.topic.not.found.driver}")
  private String notFoundDriverTopic;

  @Value("${spring.kafka.consumer.group-id.available-driver}")
  private String groupIdAvailableDriver;

  @Value("${spring.kafka.consumer.group-id.not.found.driver}")
  private String groupIdNotFoundDriver;

  @Bean
  public IntegrationFlow listenerAvailableDriver() {
    return IntegrationFlows.from(
            Kafka.messageDrivenChannelAdapter(listenerContainerGetAvailableDriver()))
        .handle(rideService, "getAvailableDriver")
        .get();
  }

  @Bean
  public IntegrationFlow listenerNotFoundDriver() {
    return IntegrationFlows.from(
            Kafka.messageDrivenChannelAdapter(listenerContainerGetNotAvailableDriver()))
        .handle(rideService, "getNotFoundDriver")
        .get();
  }

  @Bean
  public KafkaMessageListenerContainer<String, String> listenerContainerGetAvailableDriver() {
    ContainerProperties containerPropertiesAvailableDriver =
        new ContainerProperties(availableDriverTopic);
    containerPropertiesAvailableDriver.setGroupId(groupIdAvailableDriver);
    return new KafkaMessageListenerContainer<>(
        consumerFactory(), containerPropertiesAvailableDriver);
  }

  @Bean
  public KafkaMessageListenerContainer<String, String> listenerContainerGetNotAvailableDriver() {
    ContainerProperties containerPropertiesGetNotAvailableDriver =
        new ContainerProperties(notFoundDriverTopic);
    containerPropertiesGetNotAvailableDriver.setGroupId(groupIdNotFoundDriver);
    return new KafkaMessageListenerContainer<>(
        consumerFactory(), containerPropertiesGetNotAvailableDriver);
  }

  @Bean
  public Map<String, Object> consumerConfig() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES,"*");
    props.put(
        JsonDeserializer.TYPE_MAPPINGS,
        "rideSearchDto:"
            + RideSearchDto.class.getName()
            + ",com.modsen.driverservice.dto.DriverRideDto:"
            + DriverRideDto.class.getName());
    return props;
  }

  @Bean
  public ConsumerFactory<String, Object> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfig());
  }

  @Bean
  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
      kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }
}
