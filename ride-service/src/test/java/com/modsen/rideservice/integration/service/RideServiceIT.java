package com.modsen.rideservice.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.rideservice.config.kafka.KafkaProperties;
import com.modsen.rideservice.dto.CarDto;
import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.dto.DriverRideDto;
import com.modsen.rideservice.dto.DriverWithCarDto;
import com.modsen.rideservice.integration.testenvironment.IntegrationTestEnvironment;
import com.modsen.rideservice.model.Status;
import com.modsen.rideservice.repository.RideRepository;
import com.modsen.rideservice.service.RideService;
import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.testcontainers.shaded.org.awaitility.Awaitility.waitAtMost;

@RequiredArgsConstructor
class RideServiceIT extends IntegrationTestEnvironment {

  public static final Long EXIST_SECOND_RIDE_ID = 2L;
  public static final Long EXIST_DRIVER_ID = 1L;

  private final RideService rideService;
  private final Jackson2ObjectMapperBuilder builder;
  private final KafkaProperties kafkaProperties;
  private final RideRepository rideRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private DriverRideDto driverRideDto;
  private ObjectMapper objectMapper;

  @BeforeEach
  @Override
  protected void setUp() {
    super.setUp();

    CarDto carDto =
        CarDto.builder()
            .driverId(EXIST_DRIVER_ID)
            .colour("green")
            .model("Lada vesta")
            .number("1123-AC7")
            .build();

    driverRideDto =
        DriverRideDto.builder()
            .id(EXIST_DRIVER_ID)
            .rideId(EXIST_SECOND_RIDE_ID)
            .firstName("Ivan")
            .lastName("Ivanou")
            .carDto(carDto)
            .build();

    DriverPageDto driverPageDto = new DriverPageDto();
    driverPageDto.setDriverDtoList(List.of(driverRideDto));

    objectMapper = builder.build();
  }

  @Test
  void getAvailableDriverFromKafkaTopic() throws JsonProcessingException {
    MockResponse responseGetDriverWithCarDto =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(new DriverWithCarDto()));
    mockWebServer.enqueue(responseGetDriverWithCarDto);

    MockResponse responseUpdateDriverAvailabilityToTrueAfterRide =
        new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value());
    mockWebServer.enqueue(responseUpdateDriverAvailabilityToTrueAfterRide);

    kafkaTemplate.send(kafkaProperties.getTopicAvailableDriver(), driverRideDto);
    waitAtMost(15, TimeUnit.SECONDS)
        .until(
            () ->
                rideRepository.findById(EXIST_SECOND_RIDE_ID).get().getStatus() != Status.PENDING);
    Status actualStatus = rideService.getById(EXIST_SECOND_RIDE_ID).getStatus();

    assertSame(Status.ACTIVE, actualStatus);
  }

  @Test
  void getNotFoundDriverFromKafkaTopic() throws JsonProcessingException {
    MockResponse responseGetDriverWithCarDto =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(new DriverWithCarDto()));
    mockWebServer.enqueue(responseGetDriverWithCarDto);

    kafkaTemplate.send(
        kafkaProperties.getTopicNotFoundDriver(),
        DriverRideDto.builder().rideId(EXIST_SECOND_RIDE_ID).build());

    waitAtMost(15, TimeUnit.SECONDS)
        .until(
            () ->
                rideRepository.findById(EXIST_SECOND_RIDE_ID).get().getStatus() != Status.PENDING);
    Status actualStatus = rideService.getById(EXIST_SECOND_RIDE_ID).getStatus();

    assertSame(Status.NO_DRIVERS, actualStatus);
  }
}
