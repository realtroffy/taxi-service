package com.modsen.passengerservice.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.integration.testbase.IntegrationTestBase;
import com.modsen.passengerservice.model.BankCard;
import com.modsen.passengerservice.model.Passenger;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
class PassengerControllerIT extends IntegrationTestBase {

  public static final String PASSENGER_URL = "/api/v1/passengers";
  public static final String NOT_EXIST_ID = "Not exist Id";
  public static final Double DEFAULT_NEW_PASSENGER_RATING = 5.0;
  public static final String FIRST_NAME_LESS_THAN_3_SYMBOLS = "Jo";
  public static final String NOT_EXISTED_BANK_CARD_NUMBER = "1234567891011333";
  public static final String PASSENGER_COLLECTION_NAME = "passengers";
  public static final String BANK_CARD_COLLECTION_NAME = "bank-cards";

  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final WebTestClient webTestClient;
  private ObjectMapper objectMapper;
  private PassengerDto correctPassengerDto;
  private PassengerDto anotherCorrectPassengerDto;
  private PassengerDto savedPassengerDto;
  private BankCardDto bankCardDtoCorrect;

  @BeforeEach
  void setUp() {
    correctPassengerDto =
        PassengerDto.builder()
            .password("123")
            .firstName("John")
            .lastName("Doe")
            .email("uniq@google.com")
            .build();

    anotherCorrectPassengerDto =
        PassengerDto.builder()
            .password("123")
            .firstName("John")
            .lastName("Doe")
            .email("uniq2@google.com")
            .build();

    objectMapper = new ObjectMapper();

    savedPassengerDto =
        reactiveMongoTemplate.save(correctPassengerDto, PASSENGER_COLLECTION_NAME).block();

    bankCardDtoCorrect =
        BankCardDto.builder()
            .cardNumber(NOT_EXISTED_BANK_CARD_NUMBER)
            .passengerId(savedPassengerDto.getId())
            .balance(BigDecimal.TEN)
            .build();
  }

  @AfterEach
  void tearDown() {
    reactiveMongoTemplate
        .remove(Query.query(Criteria.where("_id").exists(true)), PASSENGER_COLLECTION_NAME)
        .block();

    reactiveMongoTemplate
        .remove(Query.query(Criteria.where("_id").exists(true)), BANK_CARD_COLLECTION_NAME)
        .block();
  }

  @Test
  void getPassengerByIdWhenExist() throws Exception {
    webTestClient
        .get()
        .uri(PASSENGER_URL + "/" + savedPassengerDto.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(objectMapper.writeValueAsString(savedPassengerDto));
  }

  @Test
  void getPassengerByIdWhenNotExist() {
    webTestClient
        .get()
        .uri(PASSENGER_URL + "/" + NOT_EXIST_ID)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void getAllPassengers() throws Exception {
    webTestClient
        .get()
        .uri(PASSENGER_URL)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(objectMapper.writeValueAsString(List.of(savedPassengerDto)));
  }

  @Test
  void savePassengerIfDtoCorrect() {
    webTestClient
        .post()
        .uri(PASSENGER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
        .exchange()
        .expectStatus()
        .isCreated();

    StepVerifier.create(
            reactiveMongoTemplate.findAll(Passenger.class, PASSENGER_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 2)
        .verifyComplete();
  }

  @Test
  void savePassengerIfEmailExist() {
    anotherCorrectPassengerDto.setEmail(savedPassengerDto.getEmail());

    webTestClient
        .post()
        .uri(PASSENGER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void savePassengerIfPasswordLessThan3Symbols() {
    anotherCorrectPassengerDto.setPassword("ab");

    Flux<Map<String, String>> responseBody =
        webTestClient
            .post()
            .uri(PASSENGER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .returnResult(new ParameterizedTypeReference<Map<String, String>>() {})
            .getResponseBody();

    Map<String, String> errors = new HashMap<>();
    errors.put("password", "Password should be between 3 and 30 characters");

    StepVerifier.create(responseBody).expectNext(errors).verifyComplete();
  }

  @Test
  void savePassengerIfPasswordMoreThan30Symbols() {
    anotherCorrectPassengerDto.setPassword("1234567891234567891234567912345");

    Flux<Map<String, String>> responseBody =
        webTestClient
            .post()
            .uri(PASSENGER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .returnResult(new ParameterizedTypeReference<Map<String, String>>() {})
            .getResponseBody();

    Map<String, String> errors = new HashMap<>();
    errors.put("password", "Password should be between 3 and 30 characters");

    StepVerifier.create(responseBody).expectNext(errors).verifyComplete();
  }

  @Test
  void savePassengerIfFirstNameLessThan3Symbols() {
    anotherCorrectPassengerDto.setFirstName(FIRST_NAME_LESS_THAN_3_SYMBOLS);

    Flux<Map<String, String>> responseBody =
        webTestClient
            .post()
            .uri(PASSENGER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .returnResult(new ParameterizedTypeReference<Map<String, String>>() {})
            .getResponseBody();

    Map<String, String> errors = new HashMap<>();
    errors.put("firstName", "First name should be between 3 and 30 characters");

    StepVerifier.create(responseBody).expectNext(errors).verifyComplete();
  }

  @Test
  void savePassengerIfDtoCorrectHasDefaultRating5() {
    Flux<PassengerDto> responseBody =
        webTestClient
            .post()
            .uri(PASSENGER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
            .exchange()
            .expectStatus()
            .isCreated()
            .returnResult(PassengerDto.class)
            .getResponseBody();

    StepVerifier.create(responseBody)
        .expectNextMatches(
            passengerDto -> passengerDto.getRating().equals(DEFAULT_NEW_PASSENGER_RATING))
        .expectComplete()
        .verify();
  }

  @Test
  void savePassengerIfEmailIsNull() {
    anotherCorrectPassengerDto.setEmail(null);

    Flux<Map<String, String>> responseBody =
        webTestClient
            .post()
            .uri(PASSENGER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .returnResult(new ParameterizedTypeReference<Map<String, String>>() {})
            .getResponseBody();

    Map<String, String> errors = new HashMap<>();
    errors.put("email", "must not be blank");

    StepVerifier.create(responseBody).expectNext(errors).verifyComplete();
  }

  @Test
  void savePassengerIfFirstNameIsNull() {
    anotherCorrectPassengerDto.setFirstName(null);

    Flux<Map<String, String>> responseBody =
        webTestClient
            .post()
            .uri(PASSENGER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(anotherCorrectPassengerDto), PassengerDto.class)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .returnResult(new ParameterizedTypeReference<Map<String, String>>() {})
            .getResponseBody();

    Map<String, String> errors = new HashMap<>();
    errors.put("firstName", "must not be blank");

    StepVerifier.create(responseBody).expectNext(errors).verifyComplete();
  }

  @Test
  void deletePassengerByIdIfExist() {
    webTestClient
        .delete()
        .uri(PASSENGER_URL + "/" + savedPassengerDto.getId())
        .exchange()
        .expectStatus()
        .isOk();

    StepVerifier.create(
            reactiveMongoTemplate.findAll(Passenger.class, PASSENGER_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 0)
        .verifyComplete();

    StepVerifier.create(
            reactiveMongoTemplate.findAll(BankCard.class, BANK_CARD_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 0)
        .verifyComplete();
  }

  @Test
  void deletePassengerByIdIfPassengerNotExisted() {
    reactiveMongoTemplate.save(bankCardDtoCorrect, BANK_CARD_COLLECTION_NAME).block();

    webTestClient
        .delete()
        .uri(PASSENGER_URL + "/" + NOT_EXIST_ID)
        .exchange()
        .expectStatus()
        .isNotFound();

    StepVerifier.create(
            reactiveMongoTemplate.findAll(Passenger.class, PASSENGER_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 1)
        .verifyComplete();

    StepVerifier.create(
            reactiveMongoTemplate.findAll(BankCard.class, BANK_CARD_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 1)
        .verifyComplete();
  }

  @Test
  void updatePassengerByIdIfNotExist() {
    webTestClient
        .put()
        .uri(PASSENGER_URL + "/" + NOT_EXIST_ID)
        .body(Mono.just(correctPassengerDto), PassengerDto.class)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void updatePassengerByIdIfExist() {
    savedPassengerDto.setRating(1.0);
    savedPassengerDto.setEmail("new@email.com");
    savedPassengerDto.setFirstName("Ivan");

    webTestClient
        .put()
        .uri(PASSENGER_URL + "/" + savedPassengerDto.getId())
        .body(Mono.just(correctPassengerDto), PassengerDto.class)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.rating")
        .isEqualTo(1.0)
        .jsonPath("$.email")
        .isEqualTo(savedPassengerDto.getEmail())
        .jsonPath("$.firstName")
        .isEqualTo(savedPassengerDto.getFirstName());

    StepVerifier.create(
            reactiveMongoTemplate.findAll(Passenger.class, PASSENGER_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 1)
        .verifyComplete();
  }
}
