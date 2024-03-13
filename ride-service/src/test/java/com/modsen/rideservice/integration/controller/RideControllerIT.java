package com.modsen.rideservice.integration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.rideservice.dto.BankCardDto;
import com.modsen.rideservice.dto.CarDto;
import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.dto.DriverRideDto;
import com.modsen.rideservice.dto.DriverWithCarDto;
import com.modsen.rideservice.dto.PassengerDto;
import com.modsen.rideservice.dto.PassengerRatingFinishDto;
import com.modsen.rideservice.dto.RideDto;
import com.modsen.rideservice.dto.RidePageDto;
import com.modsen.rideservice.dto.RideSearchDto;
import com.modsen.rideservice.integration.controller.restassured.RestAssuredRideController;
import com.modsen.rideservice.integration.helper.AccessTokenExtractor;
import com.modsen.rideservice.integration.testenvironment.IntegrationTestEnvironment;
import com.modsen.rideservice.model.Status;
import com.modsen.rideservice.repository.RideRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@RequiredArgsConstructor
@Slf4j
class RideControllerIT extends IntegrationTestEnvironment {

  public static final Long EXIST_PASSENGER_ID = 1L;
  public static final Long EXIST_RIDE_ID = 1L;
  public static final Long EXIST_SECOND_RIDE_ID = 2L;
  public static final Long EXIST_DRIVER_ID = 1L;
  public static final int COUNT_EXISTED_ENTITY = 4;
  public static final Integer RATING_AFTER_RIDE = 3;
  public static final Long PASSENGER_ID_WITH_NO_ACTIVE_RIDE = 5L;
  public static final Long BANK_CARD_ID_WITH_ENOUGH_MONEY = 3L;
  public static final BigDecimal ENOUGH_MONEY_BALANCE_ON_BANK_CARD = BigDecimal.valueOf(1000);
  public static final BigDecimal NOT_ENOUGH_MONEY_BALANCE_ON_BANK_CARD = BigDecimal.ZERO;
  public static final Long NEXT_RIDE_ID = 5L;
  public static final int COUNT_MESSAGES_IN_ORDER_NEW_RIDE_KAFKA_TOPIC = 1;
  public static final String PASSENGER_BANK_CARD_NOT_ENOUGH_MONEY_EXCEPTION_MESSAGE =
      "Not enough money on your bank card. Choose another bank card to pay or pay cash";
  public static final String PASSENGER_NOT_FOUND_EXCEPTION_MESSAGE =
      "Passenger was not found by such id = ";
  public static final String PASSENGER_BANK_CARD_NOT_FOUND_EXCEPTION_MESSAGE =
      "Passenger bank card was not found by such id = ";
  public static final String PASSENGER_HAVE_UNFINISHED_RIDE_EXCEPTION_MESSAGE =
      "You have unfinished ride. You could order new ride after finished current ride";
  public static final String KAFKA_ORDER_RIDE_TOPIC = "order-new-ride";

  private final RideRepository rideRepository;
  private final Jackson2ObjectMapperBuilder builder;
  private final Consumer<String, Object> testConsumer;
  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final RetryRegistry retryRegistry;
  private final AccessTokenExtractor accessTokenExtractor;
  private final RestAssuredRideController restAssured;

  private RideDto rideDtoCorrect;
  private RideDto savedInDbRideDto;
  private CarDto carDto;
  private DriverWithCarDto driverWithCarDto;
  private DriverPageDto driverPageDto;
  private ObjectMapper objectMapper;
  private CircuitBreaker rideServiceCircuitBreaker;
  private CircuitBreakerConfig rideServiceCircuitBreakerConfig;
  private RetryConfig rideServiceRetryConfig;
  private String adminAccessToken;
  private String passengerAccessToken;
  private String driverAccessToken;

  @BeforeEach
  @Override
  protected void setUp() {
    super.setUp();
    rideDtoCorrect =
        RideDto.builder()
            .startLocation("Minsk")
            .endLocation("London")
            .passengerId(EXIST_PASSENGER_ID)
            .driverId(EXIST_DRIVER_ID)
            .bookingTime(LocalDateTime.of(2023, 10, 10, 10, 10))
            .passengerBankCardId("3")
            .cost(BigDecimal.valueOf(10.12))
            .build();

    carDto =
        CarDto.builder()
            .driverId(EXIST_DRIVER_ID)
            .colour("green")
            .model("Lada vesta")
            .number("1123-AC7")
            .build();

    savedInDbRideDto =
        RideDto.builder()
            .id(EXIST_RIDE_ID)
            .startLocation("Minsk")
            .endLocation("London")
            .passengerId(EXIST_PASSENGER_ID)
            .driverId(EXIST_DRIVER_ID)
            .bookingTime(LocalDateTime.of(2023, 1, 1, 10, 0))
            .approvedTime(LocalDateTime.of(2023, 1, 1, 10, 1))
            .startTime(LocalDateTime.of(2023, 1, 1, 10, 1))
            .promoCodeName("SUPER50")
            .status(Status.ACTIVE)
            .carDto(carDto)
            .passengerBankCardId("1")
            .cost(BigDecimal.valueOf(18.79))
            .build();

    DriverRideDto driverRideDto =
        DriverRideDto.builder()
            .id(EXIST_DRIVER_ID)
            .rideId(EXIST_RIDE_ID)
            .firstName("Ivan")
            .lastName("Ivanou")
            .carDto(carDto)
            .build();

    driverPageDto = new DriverPageDto();
    driverPageDto.setDriverDtoList(List.of(driverRideDto));

    driverWithCarDto = DriverWithCarDto.builder().carDto(carDto).build();

    objectMapper = builder.build();

    rideServiceCircuitBreaker = circuitBreakerRegistry.circuitBreaker("CircuitBreakerRideService");
    rideServiceCircuitBreakerConfig = rideServiceCircuitBreaker.getCircuitBreakerConfig();
    rideServiceCircuitBreaker.reset();

    Retry rideServiceRetry = retryRegistry.retry("retryRideService");
    rideServiceRetryConfig = rideServiceRetry.getRetryConfig();

    adminAccessToken = accessTokenExtractor.getAdminAccessToken();
    passengerAccessToken = accessTokenExtractor.getPassengerAccessToken();
    driverAccessToken = accessTokenExtractor.getDriverAccessToken();
  }

  @Test
  void getRideByIdIfExist() throws JsonProcessingException {
    MockResponse responseGetDriverWithCarDto =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(driverWithCarDto));
    mockWebServer.enqueue(responseGetDriverWithCarDto);

    Response response = restAssured.getRideIdIfExist(adminAccessToken);

    RideDto actual = response.then().statusCode(HttpStatus.OK.value()).extract().as(RideDto.class);

    assertEquals(savedInDbRideDto, actual);
  }

  @Test
  void getRideByIdIfNotExist() {
    Response response = restAssured.getRideByIdIfNotExist(adminAccessToken);

    response.then().statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void getAllRides() throws JsonProcessingException {
    MockResponse responseGetDriverPageDto =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(driverPageDto));

    mockWebServer.enqueue(responseGetDriverPageDto);

    Response response = restAssured.getAllRides(adminAccessToken);

    RidePageDto actual =
        response.then().statusCode(HttpStatus.OK.value()).extract().as(RidePageDto.class);
    assertEquals(COUNT_EXISTED_ENTITY, actual.getRideDtoList().size());
  }

  @Test
  void deleteRideByIdIfExist() {
    Response response = restAssured.deleteRideByIdIfExist(adminAccessToken);

    long actualCountRidesAfterDeleting =
        StreamSupport.stream(rideRepository.findAll().spliterator(), false).count();

    response.then().statusCode(HttpStatus.NO_CONTENT.value());
    assertEquals(COUNT_EXISTED_ENTITY - 1, actualCountRidesAfterDeleting);
  }

  @Test
  void deleteRideByIdIfNotExist() {
    Response response = restAssured.deleteRideByIdIfNotExist(adminAccessToken);
    long actualCountRidesAfterDeleting =
        StreamSupport.stream(rideRepository.findAll().spliterator(), false).count();

    response.then().statusCode(HttpStatus.NOT_FOUND.value());
    assertEquals(COUNT_EXISTED_ENTITY, actualCountRidesAfterDeleting);
  }

  @Test
  void updateRideByIdIfExist() throws JsonProcessingException {
    rideDtoCorrect.setStatus(Status.PENDING);
    rideDtoCorrect.setId(EXIST_RIDE_ID);
    rideDtoCorrect.setCarDto(carDto);

    MockResponse responseGetDriverWithCarDto =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(driverWithCarDto));
    mockWebServer.enqueue(responseGetDriverWithCarDto);

    Response response =
        restAssured.updateRideByIdIfExist(adminAccessToken, rideDtoCorrect, objectMapper);
    RideDto actual = restAssured.getRideIdIfExist(adminAccessToken).as(RideDto.class);

    response.then().statusCode(HttpStatus.NO_CONTENT.value());
    assertEquals(rideDtoCorrect, actual);
  }

  @Test
  void updateRideByIdIfNotExist() throws JsonProcessingException {
    Response response =
        restAssured.updateRideByIdIfNotExist(adminAccessToken, rideDtoCorrect, objectMapper);

    response.then().statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void updateDriverRatingAfterFinishRideIfExist() {
    MockResponse responseUpdateDriverRatingAfterFinishRide =
        new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value());
    mockWebServer.enqueue(responseUpdateDriverRatingAfterFinishRide);

    Response response = restAssured.updateDriverRatingAfterFinishRideIfExist(adminAccessToken);

    response.then().statusCode(HttpStatus.NO_CONTENT.value());
  }

  @Test
  void updateDriverRatingAfterFinishRideIfNotExist() {
    Response response = restAssured.updateDriverRatingAfterFinishRideIfNotExist(adminAccessToken);

    response.then().statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void cancelByPassengerIfRideExistAndStatusPending() {
    Response response = restAssured.cancelByPassengerIfRideExistAndStatusPending(adminAccessToken);

    response
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .and()
        .body("id", equalTo(EXIST_SECOND_RIDE_ID.intValue()))
        .body("status", equalTo("CANCELED"));
  }

  @Test
  void cancelByPassengerIfRideNotExist() {
    Response response = restAssured.cancelByPassengerIfRideNotExist(adminAccessToken);

    response.then().statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @Sql(value = "classpath:db/change-ride-status-to-active.sql")
  @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
  void cancelByPassengerIfRideExistAndStatusActiveThenReturnStatusBadRequest() {
    Response response =
        restAssured.cancelByPassengerIfRideExistAndStatusActiveThenReturnStatusBadRequest(
            adminAccessToken);

    response.then().statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void finishByDriverIfRideExist() throws JsonProcessingException {
    PassengerRatingFinishDto passengerRatingFinishDto = new PassengerRatingFinishDto();
    passengerRatingFinishDto.setPassengerRating(RATING_AFTER_RIDE);

    MockResponse responseUpdatePassengerAfterRide = new MockResponse().setResponseCode(204);
    mockWebServer.enqueue(responseUpdatePassengerAfterRide);

    MockResponse responseUpdateDriverAvailabilityToTrueAfterRide =
        new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value());
    mockWebServer.enqueue(responseUpdateDriverAvailabilityToTrueAfterRide);

    Response response =
        restAssured.finishByDriverIfRideExist(
            adminAccessToken, objectMapper, passengerRatingFinishDto);

    response.then().statusCode(HttpStatus.NO_CONTENT.value());
  }

  @Test
  void finishByDriverIfRideNotExist() throws JsonProcessingException {
    PassengerRatingFinishDto passengerRatingFinishDto = new PassengerRatingFinishDto();
    passengerRatingFinishDto.setPassengerRating(RATING_AFTER_RIDE);

    Response response =
        restAssured.finishByDriverIfRideNotExist(
            adminAccessToken, objectMapper, passengerRatingFinishDto);

    response.then().statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void orderRideWhenPassengerExistAndHaveNotUnfinishedRideAndHaveEnoughMoneyOnBankCard()
      throws JsonProcessingException {
    BankCardDto bankCardDtoWithEnoughMoney =
        BankCardDto.builder()
            .id(BANK_CARD_ID_WITH_ENOUGH_MONEY)
            .balance(ENOUGH_MONEY_BALANCE_ON_BANK_CARD)
            .build();

    PassengerDto passengerDto =
        PassengerDto.builder().bankCards(List.of(bankCardDtoWithEnoughMoney)).build();

    MockResponse responseFindPassengerById =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(passengerDto));

    mockWebServer.enqueue(responseFindPassengerById);

    rideDtoCorrect.setPassengerId(PASSENGER_ID_WITH_NO_ACTIVE_RIDE);
    RideSearchDto expected = RideSearchDto.builder().rideId(NEXT_RIDE_ID).build();

    Response response = restAssured.orderRide(adminAccessToken, objectMapper, rideDtoCorrect);

    RideDto actualRideDto =
        response.then().statusCode(HttpStatus.CREATED.value()).extract().as(RideDto.class);
    Status actual = actualRideDto.getStatus();

    testConsumer.subscribe(List.of(KAFKA_ORDER_RIDE_TOPIC));
    ConsumerRecords<String, Object> records = testConsumer.poll(Duration.ofMillis(10000));
    testConsumer.close();
    int actualMessageCountInKafkaOrderNewRideTopic = records.count();
    RideSearchDto actualRideSearchDtoFromKafkaTopic =
        (RideSearchDto) records.iterator().next().value();

    assertEquals(expected, actualRideSearchDtoFromKafkaTopic);
    assertEquals(
        COUNT_MESSAGES_IN_ORDER_NEW_RIDE_KAFKA_TOPIC, actualMessageCountInKafkaOrderNewRideTopic);
    assertNotNull(actualRideDto);
    assertSame(Status.PENDING, actual);
  }

  @Test
  void orderRideWhenPassengerNotExist() throws JsonProcessingException {
    rideDtoCorrect.setPassengerId(PASSENGER_ID_WITH_NO_ACTIVE_RIDE);

    MockResponse responseNotFoundPassengerById = new MockResponse().setResponseCode(404);

    mockWebServer.enqueue(responseNotFoundPassengerById);

    Response response = restAssured.orderRide(adminAccessToken, objectMapper, rideDtoCorrect);

    Response actualResponse =
        response.then().statusCode(HttpStatus.NOT_FOUND.value()).extract().response();
    String actual = actualResponse.getBody().asString();

    assertEquals(PASSENGER_NOT_FOUND_EXCEPTION_MESSAGE + PASSENGER_ID_WITH_NO_ACTIVE_RIDE, actual);
  }

  @Test
  void orderRideWhenPassengerExistAndBankCardNotExistOrIncorrect() throws JsonProcessingException {
    rideDtoCorrect.setPassengerId(PASSENGER_ID_WITH_NO_ACTIVE_RIDE);
    PassengerDto passengerDtoWithEmptyBankCardList =
        PassengerDto.builder().bankCards(Collections.emptyList()).build();

    MockResponse responseFindPassengerByIdWithEmptyBankCardList =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(passengerDtoWithEmptyBankCardList));

    mockWebServer.enqueue(responseFindPassengerByIdWithEmptyBankCardList);

    Response response = restAssured.orderRide(adminAccessToken, objectMapper, rideDtoCorrect);

    Response actualResponse =
        response.then().statusCode(HttpStatus.NOT_FOUND.value()).extract().response();
    String actual = actualResponse.getBody().asString();

    assertEquals(
        PASSENGER_BANK_CARD_NOT_FOUND_EXCEPTION_MESSAGE + rideDtoCorrect.getPassengerBankCardId(),
        actual);
  }

  @Test
  void orderRideWhenPassengerExistAndHaveNotActiveRideAndHaveNotEnoughMoneyOnBankCard()
      throws JsonProcessingException {
    rideDtoCorrect.setPassengerId(PASSENGER_ID_WITH_NO_ACTIVE_RIDE);
    BankCardDto bankCardDtoWithNotEnoughMoney =
        BankCardDto.builder()
            .id(BANK_CARD_ID_WITH_ENOUGH_MONEY)
            .balance(NOT_ENOUGH_MONEY_BALANCE_ON_BANK_CARD)
            .build();
    PassengerDto passengerDtoWithNotEnoughMoneyOnBankCard =
        PassengerDto.builder().bankCards(List.of(bankCardDtoWithNotEnoughMoney)).build();

    MockResponse responseFindPassengerByIdWithNotEnoughMoneyOnBankCard =
        new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(objectMapper.writeValueAsString(passengerDtoWithNotEnoughMoneyOnBankCard));

    mockWebServer.enqueue(responseFindPassengerByIdWithNotEnoughMoneyOnBankCard);

    Response response = restAssured.orderRide(adminAccessToken, objectMapper, rideDtoCorrect);

    Response actualResponse =
        response.then().statusCode(HttpStatus.BAD_REQUEST.value()).extract().response();
    String actual = actualResponse.getBody().asString();

    assertEquals(PASSENGER_BANK_CARD_NOT_ENOUGH_MONEY_EXCEPTION_MESSAGE, actual);
  }

  @Test
  void orderRideWhenPassengerHaveUnfinishedRide() throws JsonProcessingException {
    rideDtoCorrect.setPassengerId(EXIST_PASSENGER_ID);

    Response response = restAssured.orderRide(adminAccessToken, objectMapper, rideDtoCorrect);

    Response actualResponse =
        response.then().statusCode(HttpStatus.BAD_REQUEST.value()).extract().response();
    String actual = actualResponse.getBody().asString();

    assertEquals(PASSENGER_HAVE_UNFINISHED_RIDE_EXCEPTION_MESSAGE, actual);
  }

  @Test
  void testRetryAndCircuitBreakerCombination() {
    rideDtoCorrect.setPassengerId(PASSENGER_ID_WITH_NO_ACTIVE_RIDE);

    MockResponse errorResponse =
        new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

    // from close to open state
    IntStream.rangeClosed(1, rideServiceCircuitBreakerConfig.getMinimumNumberOfCalls())
        .forEach(
            i -> {
              addResponseToMockWebServer(errorResponse);
              Response response = getResponse();
              assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            });
    assertSame(CircuitBreaker.State.OPEN, rideServiceCircuitBreaker.getState());

    // open state
    IntStream.rangeClosed(1, 3)
        .forEach(
            i -> {
              Response response = getResponse();
              assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getStatusCode());
            });

    // wait for change state from open to half-open
    Awaitility.await()
        .until(() -> rideServiceCircuitBreaker.getState() == CircuitBreaker.State.HALF_OPEN);

    // from half-open to open state
    IntStream.rangeClosed(
            1, rideServiceCircuitBreakerConfig.getPermittedNumberOfCallsInHalfOpenState())
        .forEach(
            i -> {
              addResponseToMockWebServer(errorResponse);
              Response response = getResponse();
              assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            });
  }

  private void addResponseToMockWebServer(MockResponse errorResponse) {
    for (int i = 0; i < rideServiceRetryConfig.getMaxAttempts(); i++) {
      mockWebServer.enqueue(errorResponse);
    }
  }

  @SneakyThrows
  private Response getResponse() {
    return restAssured
        .orderRide(adminAccessToken, objectMapper, rideDtoCorrect)
        .then()
        .extract()
        .response();
  }

  @Test
  void couldNotDeleteRideByPassengerRole() {
    restAssured
        .deleteRideByIdIfExist(passengerAccessToken)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void couldNotOrderRideByDriverRole() throws JsonProcessingException {
    restAssured
        .orderRide(driverAccessToken, objectMapper, rideDtoCorrect)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void couldNotOrderRideWithoutAccessToken() throws JsonProcessingException {
    restAssured
        .orderRideWithoutAccessToken(objectMapper, rideDtoCorrect)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  void couldNotOrderRideWithIncorrectAccessToken() throws JsonProcessingException {
    restAssured
        .orderRide("incorrect token value", objectMapper, rideDtoCorrect)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }
}
