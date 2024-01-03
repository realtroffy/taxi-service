package com.modsen.driverservice.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.driverservice.config.kafka.KafkaProperties;
import com.modsen.driverservice.dto.DriverRideDto;
import com.modsen.driverservice.dto.RideSearchDto;
import com.modsen.driverservice.exception.RideSearchDtoMappingException;
import com.modsen.driverservice.integration.testbase.IntegrationTestBase;
import com.modsen.driverservice.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
class DriverServiceIT extends IntegrationTestBase {

  public static final Long EXIST_DRIVER_ID = 22L;
  public static final String EXIST_DRIVER_NAME = "John";
  public static final Long RANDOM_RIDE_ID = 1L;

  private final KafkaProperties kafkaProperties;
  private final DriverService driverService;
  private final StreamsBuilder streamsBuilder;
  private TopologyTestDriver topologyTestDriver;
  private ObjectMapper objectMapper;
  private TestOutputTopic<String, String> availableDriverOutputTopic;
  private TestOutputTopic<String, String> notFoundDriverOutputTopic;

  @BeforeEach
  protected void setUp() {
    topologyTestDriver =
        new TopologyTestDriver(
            driverService.getAvailableRandomDriverIfExistAndChangeAvailabilityToFalse(
                streamsBuilder));

    RideSearchDto rideSearchDto = RideSearchDto.builder().rideId(RANDOM_RIDE_ID).build();
    objectMapper = new ObjectMapper();

    Serializer<String> keySerializer = Serdes.String().serializer();
    Serializer<String> valueSerializer = Serdes.String().serializer();
    Deserializer<String> keyDeserializer = Serdes.String().deserializer();
    Deserializer<String> valueDeserializer = Serdes.String().deserializer();

    TestInputTopic<String, String> orderNewRideInputTopic =
        topologyTestDriver.createInputTopic(kafkaProperties.getTopicOrderNewRide(), keySerializer, valueSerializer);

    try {
      orderNewRideInputTopic.pipeInput(objectMapper.writeValueAsString(rideSearchDto));
    } catch (JsonProcessingException exception) {
      throw new RideSearchDtoMappingException(
          "Error processing while converting rideSearchDto to String");
    }

    availableDriverOutputTopic =
        topologyTestDriver.createOutputTopic(
            kafkaProperties.getTopicAvailableDriver(), keyDeserializer, valueDeserializer);
    notFoundDriverOutputTopic =
        topologyTestDriver.createOutputTopic(
            kafkaProperties.getTopicNotFoundDriver(), keyDeserializer, valueDeserializer);
  }

  @AfterEach
  void tearDown() {
    topologyTestDriver.close();
  }

  @Test
  void whenFoundAvailableDriverThanPutMessageInAvailableTopicKafka()
      throws JsonProcessingException {
    String driverRideDtoAsStringFromAvailableKafkaTopic = availableDriverOutputTopic.readValue();
    DriverRideDto availableDriverRideDto =
        objectMapper.readValue(driverRideDtoAsStringFromAvailableKafkaTopic, DriverRideDto.class);

    assertEquals(EXIST_DRIVER_ID, availableDriverRideDto.getId());
    assertEquals(EXIST_DRIVER_NAME, availableDriverRideDto.getFirstName());
    assertEquals(RANDOM_RIDE_ID, availableDriverRideDto.getRideId());
  }

  @Test
  @Sql("classpath:db/change-driver-availability-to-false.sql")
  @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
  void whenNotFoundAvailableDriverThanPutMessageInNotFoundTopicKafka()
      throws JsonProcessingException {
    String driverRideDtoAsStringFromNotFoundKafkaTopic = notFoundDriverOutputTopic.readValue();
    DriverRideDto notFoundDriverRideDto =
        objectMapper.readValue(driverRideDtoAsStringFromNotFoundKafkaTopic, DriverRideDto.class);

    assertNull(notFoundDriverRideDto.getId());
    assertNull(notFoundDriverRideDto.getFirstName());
    assertEquals(RANDOM_RIDE_ID, notFoundDriverRideDto.getRideId());
    assertTrue(availableDriverOutputTopic.isEmpty());
  }
}
