package com.modsen.driverservice.integration.controller;

import com.modsen.driverservice.dto.BankCardDto;
import com.modsen.driverservice.dto.BankCardPageDto;
import com.modsen.driverservice.integration.testbase.IntegrationTestBase;
import com.modsen.driverservice.repository.BankCardRepository;
import com.modsen.driverservice.service.BankCardService;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
class BankCardControllerIT extends IntegrationTestBase {

  public static final String BANK_CARD_URL = "/api/v1/bankcards";
  public static final String ID_VARIABLE = "/{id}";
  public static final Long EXIST_BANK_CARD_ID = 1L;
  public static final Long EXIST_DRIVER_ID = 22L;
  public static final Long NOT_EXIST_ID = 30L;
  public static final BigDecimal EXISTED_BALANCE = BigDecimal.valueOf(100);
  public static final int COUNT_EXISTED_ENTITY = 3;
  public static final String EXISTED_BANK_CARD_NUMBER = "1234567891011131";
  public static final String NOT_EXISTED_BANK_CARD_NUMBER = "1234567891011333";

  private final BankCardService bankCardService;
  private final BankCardRepository bankCardRepository;
  private BankCardDto bankCardDtoCorrect;

  @BeforeEach
  @Override
  protected void setUp() {
    super.setUp();

    bankCardDtoCorrect =
        BankCardDto.builder()
            .cardNumber(NOT_EXISTED_BANK_CARD_NUMBER)
            .driverId(EXIST_DRIVER_ID)
            .balance(BigDecimal.TEN)
            .isDefault(false)
            .build();
  }

  @Test
  void getBankCardByIdIfExist() {
    given()
        .pathParam("id", EXIST_BANK_CARD_ID)
        .when()
        .get(BANK_CARD_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.OK.value())
        .and()
        .body("id", equalTo(EXIST_BANK_CARD_ID.intValue()))
        .body("driverId", equalTo(EXIST_DRIVER_ID.intValue()))
        .body("cardNumber", equalTo(EXISTED_BANK_CARD_NUMBER))
        .body("balance", equalTo(EXISTED_BALANCE.intValue()))
        .body("isDefault", equalTo(true));
  }

  @Test
  void getBankCardByIdIfNotExist() {
    given()
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .get(BANK_CARD_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void getAllBankCards() {
    BankCardPageDto actual =
        given()
            .when()
            .get(BANK_CARD_URL)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(BankCardPageDto.class);

    assertEquals(COUNT_EXISTED_ENTITY, actual.getBankCardDtoList().size());
  }

  @Test
  void saveBankCardIfDtoCorrect() {
    BankCardDto actual =
        given()
            .contentType(ContentType.JSON)
            .body(bankCardDtoCorrect)
            .when()
            .post(BANK_CARD_URL)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(BankCardDto.class);
    bankCardDtoCorrect.setId(4L);

    assertEquals(bankCardDtoCorrect, actual);
  }

  @Test
  void saveBankCardIfDtoWithNotExistedDriverId() {
    bankCardDtoCorrect.setDriverId(NOT_EXIST_ID);
    given()
        .contentType(ContentType.JSON)
        .body(bankCardDtoCorrect)
        .when()
        .post(BANK_CARD_URL)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void saveBankCardIfDtoWithNegativeBalance() {
    bankCardDtoCorrect.setBalance(BigDecimal.valueOf(-10));
    given()
        .contentType(ContentType.JSON)
        .body(bankCardDtoCorrect)
        .when()
        .post(BANK_CARD_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void updateBankCardByIdIfExist() {
    given()
        .pathParam("id", EXIST_BANK_CARD_ID)
        .contentType(ContentType.JSON)
        .body(bankCardDtoCorrect)
        .when()
        .put(BANK_CARD_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    BankCardDto actual = bankCardService.getById(EXIST_BANK_CARD_ID);
    bankCardDtoCorrect.setId(EXIST_BANK_CARD_ID);

    assertEquals(bankCardDtoCorrect, actual);
  }

  @Test
  void updateBankCardByIdIfNotExist() {
    given()
        .pathParam("id", NOT_EXIST_ID)
        .contentType(ContentType.JSON)
        .body(bankCardDtoCorrect)
        .when()
        .put(BANK_CARD_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void deleteBankCardByIdIfBankCardExisted() {
    given()
        .pathParam("id", EXIST_BANK_CARD_ID)
        .when()
        .delete(BANK_CARD_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    long actualCountBankCardsAfterDeleting =
        StreamSupport.stream(bankCardRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY - 1, actualCountBankCardsAfterDeleting);
  }

  @Test
  void deleteBankCardByIdIfBankCardNotExisted() {
    given()
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .delete(BANK_CARD_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
    long actualCountBankCardsAfterDeleting =
        StreamSupport.stream(bankCardRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY, actualCountBankCardsAfterDeleting);
  }
}
