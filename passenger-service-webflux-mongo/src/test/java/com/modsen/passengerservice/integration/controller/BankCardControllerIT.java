package com.modsen.passengerservice.integration.controller;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.integration.testbase.IntegrationTestBase;
import com.modsen.passengerservice.model.BankCard;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@AutoConfigureWebTestClient
class BankCardControllerIT extends IntegrationTestBase {

  public static final String BANK_CARD_URL = "/api/v1/bankcards";
  public static final String EXIST_DRIVER_ID = "222222222222222222222222";
  public static final String NOT_EXIST_ID = "not exist";
  public static final String NOT_EXISTED_BANK_CARD_NUMBER = "1234567891011333";
  public static final String NOT_EXISTED_BANK_CARD_NUMBER_SECOND = "1234567891011332";
  public static final String BANK_CARD_COLLECTION_NAME = "bank-cards";

  private final WebTestClient webTestClient;
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private ObjectMapper objectMapper;
  private BankCardDto bankCardDtoCorrect;
  private BankCardDto savedBankCardDto;
  private BankCardDto anotherBankCardDtoCorrect;

  @BeforeEach
  protected void setUp() {
    bankCardDtoCorrect =
        BankCardDto.builder()
            .cardNumber(NOT_EXISTED_BANK_CARD_NUMBER)
            .passengerId(EXIST_DRIVER_ID)
            .balance(BigDecimal.TEN)
            .build();

    anotherBankCardDtoCorrect =
        BankCardDto.builder()
            .cardNumber(NOT_EXISTED_BANK_CARD_NUMBER_SECOND)
            .passengerId(EXIST_DRIVER_ID)
            .balance(BigDecimal.ONE)
            .build();

    objectMapper = new ObjectMapper();

    savedBankCardDto =
        reactiveMongoTemplate.save(bankCardDtoCorrect, BANK_CARD_COLLECTION_NAME).block();
  }

  @AfterEach
  void tearDown() {
    reactiveMongoTemplate
        .remove(Query.query(Criteria.where("_id").exists(true)), BANK_CARD_COLLECTION_NAME)
        .block();
  }

  @Test
  void getBankCardByIdIfExist() throws JsonProcessingException {
    webTestClient
        .get()
        .uri(BANK_CARD_URL + "/" + savedBankCardDto.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(objectMapper.writeValueAsString(savedBankCardDto));
  }

  @Test
  void getBankCardByIdIfNotExist() {
    webTestClient
        .get()
        .uri(BANK_CARD_URL + "/" + NOT_EXIST_ID)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void getAllBankCards() throws JsonProcessingException {
    webTestClient
        .get()
        .uri(BANK_CARD_URL)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(objectMapper.writeValueAsString(List.of(bankCardDtoCorrect)));
  }

  @Test
  void saveBankCardIfDtoCorrect() {
    webTestClient
        .post()
        .uri(BANK_CARD_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(anotherBankCardDtoCorrect), BankCardDto.class)
        .exchange()
        .expectStatus()
        .isCreated();

    StepVerifier.create(
            reactiveMongoTemplate.findAll(BankCard.class, BANK_CARD_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 2)
        .verifyComplete();
  }

  @Test
  void saveBankCardIfDtoWithNegativeBalance() {
    bankCardDtoCorrect.setBalance(BigDecimal.valueOf(-10));

    webTestClient
        .post()
        .uri(BANK_CARD_URL)
        .body(Mono.just(bankCardDtoCorrect), BankCardDto.class)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void saveBankCardIfCardNumberAlreadyExist() {
    anotherBankCardDtoCorrect.setCardNumber(NOT_EXISTED_BANK_CARD_NUMBER);

    webTestClient
        .post()
        .uri(BANK_CARD_URL)
        .body(Mono.just(anotherBankCardDtoCorrect), BankCardDto.class)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void updateBankCardByIdIfExist() {
    savedBankCardDto.setCardNumber("1234567891011121");
    savedBankCardDto.setBalance(BigDecimal.ZERO);
    savedBankCardDto.setPassengerId(new ObjectId().toHexString());

    webTestClient
        .put()
        .uri(BANK_CARD_URL + "/" + savedBankCardDto.getId())
        .body(Mono.just(bankCardDtoCorrect), BankCardDto.class)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.balance")
        .isEqualTo(BigDecimal.ZERO)
        .jsonPath("$.cardNumber")
        .isEqualTo(savedBankCardDto.getCardNumber())
        .jsonPath("$.passengerId")
        .isEqualTo(savedBankCardDto.getPassengerId());

    StepVerifier.create(
            reactiveMongoTemplate.findAll(BankCard.class, BANK_CARD_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 1)
        .verifyComplete();
  }

  @Test
  void updateBankCardByIdIfNotExist() {
    savedBankCardDto.setId(NOT_EXIST_ID);

    webTestClient
        .put()
        .uri(BANK_CARD_URL + "/" + savedBankCardDto.getId())
        .body(Mono.just(bankCardDtoCorrect), BankCardDto.class)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void deleteBankCardByIdIfBankCardExisted() {
    webTestClient
        .delete()
        .uri(BANK_CARD_URL + "/" + savedBankCardDto.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.balance")
        .isEqualTo(savedBankCardDto.getBalance())
        .jsonPath("$.cardNumber")
        .isEqualTo(savedBankCardDto.getCardNumber())
        .jsonPath("$.passengerId")
        .isEqualTo(savedBankCardDto.getPassengerId());

    StepVerifier.create(
            reactiveMongoTemplate.findAll(BankCard.class, BANK_CARD_COLLECTION_NAME).collectList())
        .expectNextMatches(list -> list.size() == 0)
        .verifyComplete();
  }

  @Test
  void deleteBankCardByIdIfBankCardNotExisted() {
    webTestClient
        .delete()
        .uri(BANK_CARD_URL + "/" + NOT_EXIST_ID)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
