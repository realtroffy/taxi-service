package com.modsen.rideservice.config.kafka;

import com.modsen.rideservice.dto.DriverRideDto;
import com.modsen.rideservice.service.RideService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

  public static final String FULL_CLASS_NAME_DRIVER_DTO_IN_DRIVER_SERVICE =
      "com.modsen.driverservice.dto.DriverRideDto";
  private final RideService rideService;
  private final KafkaProperties kafkaProperties;

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
        new ContainerProperties(kafkaProperties.getTopicAvailableDriver());
    containerPropertiesAvailableDriver.setGroupId(
        kafkaProperties.getConsumersGroupIdAvailableDriver());
    return new KafkaMessageListenerContainer<>(
        consumerFactory(), containerPropertiesAvailableDriver);
  }

  @Bean
  public KafkaMessageListenerContainer<String, String> listenerContainerGetNotAvailableDriver() {
    ContainerProperties containerPropertiesGetNotAvailableDriver =
        new ContainerProperties(kafkaProperties.getTopicNotFoundDriver());
    containerPropertiesGetNotAvailableDriver.setGroupId(
        kafkaProperties.getConsumersGroupIdNotFoundAvailableDriver());
    return new KafkaMessageListenerContainer<>(
        consumerFactory(), containerPropertiesGetNotAvailableDriver);
  }

  @Bean
  @Profile("dev")
  public Map<String, Object> consumerConfig() {
    return Map.of(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapAddress(),
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
        JsonDeserializer.TRUSTED_PACKAGES, "*",
        JsonDeserializer.TYPE_MAPPINGS,
            FULL_CLASS_NAME_DRIVER_DTO_IN_DRIVER_SERVICE + ":" + DriverRideDto.class.getName());
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
