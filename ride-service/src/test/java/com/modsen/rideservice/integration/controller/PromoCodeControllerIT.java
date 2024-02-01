package com.modsen.rideservice.integration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.rideservice.dto.PromoCodeDto;
import com.modsen.rideservice.dto.PromoCodePageDto;
import com.modsen.rideservice.integration.helper.AccessTokenExtractor;
import com.modsen.rideservice.integration.testenvironment.IntegrationTestEnvironment;
import com.modsen.rideservice.repository.PromoCodeRepository;
import com.modsen.rideservice.service.PromoCodeService;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
class PromoCodeControllerIT extends IntegrationTestEnvironment {

  public static final String PROMO_CODE_URL = "/api/v1/promocodes";
  public static final String ID_VARIABLE = "/{id}";
  public static final Long EXIST_PROMO_CODE_ID = 1L;
  public static final Long EXIST_PROMO_CODE_ID_WITH_NO_RIDES = 2L;
  public static final Long NOT_EXIST_ID = 30L;
  public static final int COUNT_EXISTED_ENTITY = 2;
  public static final Long GENERATED_ID_AFTER_SAVE = 3L;
  public static final LocalDateTime FINISH_DATE_BEFORE_START_DATE =
      LocalDateTime.of(2020, 10, 10, 10, 10);
  public static final String FINISH_DATE_EARLY_THAN_START_DATE_EXCEPTION_MESSAGE =
      "Date from {%s} can't be after the date to {%s}";

  private final PromoCodeService promoCodeService;
  private final PromoCodeRepository promoCodeRepository;
  private final Jackson2ObjectMapperBuilder builder;
  private final AccessTokenExtractor accessTokenExtractor;
  private PromoCodeDto promoCodeDtoCorrect;
  private ObjectMapper objectMapper;
  private String adminAccessToken;
  private String passengerAccessToken;
  private String driverAccessToken;

  @BeforeEach
  @Override
  protected void setUp() {
    super.setUp();

    promoCodeDtoCorrect =
        PromoCodeDto.builder()
            .name("SUPER20")
            .discount(BigDecimal.valueOf(0.2))
            .start(LocalDateTime.of(2022, 10, 10, 10, 10))
            .end(LocalDateTime.of(2024, 10, 10, 10, 10))
            .build();

    objectMapper = builder.build();

    adminAccessToken = accessTokenExtractor.getAdminAccessToken();
    passengerAccessToken = accessTokenExtractor.getPassengerAccessToken();
    driverAccessToken = accessTokenExtractor.getDriverAccessToken();
  }

  @Test
  void getPromoCodeByIdIfExist() {
    given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_PROMO_CODE_ID)
        .when()
        .get(PROMO_CODE_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.OK.value())
        .and()
        .body("id", equalTo(EXIST_PROMO_CODE_ID.intValue()))
        .body("name", equalTo("SUPER50"))
        .body("discount", equalTo(0.5f))
        .body("start", equalTo("2022-01-01T10:00:00"))
        .body("end", equalTo("2024-01-01T10:00:00"));
  }

  @Test
  void getPromoCodeByIdIfNotExist() {
    given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .get(PROMO_CODE_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void getAllPromocodes() {
    PromoCodePageDto actualPromoCodePageDto =
        given()
            .auth()
            .oauth2(adminAccessToken)
            .when()
            .get(PROMO_CODE_URL)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(PromoCodePageDto.class);
    int actual = actualPromoCodePageDto.getPromoCodeDtoList().size();

    assertEquals(COUNT_EXISTED_ENTITY, actual);
  }

  @Test
  void saveDriverIfDtoCorrect() throws JsonProcessingException {
    PromoCodeDto actual =
        given()
            .auth()
            .oauth2(adminAccessToken)
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
            .when()
            .post(PROMO_CODE_URL)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(PromoCodeDto.class);
    PromoCodeDto expected = promoCodeService.getById(GENERATED_ID_AFTER_SAVE);

    assertEquals(expected, actual);
  }

  @Test
  void updateDriverByIdIfExist() throws JsonProcessingException {
    promoCodeDtoCorrect.setId(EXIST_PROMO_CODE_ID);
    given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_PROMO_CODE_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
        .when()
        .put(PROMO_CODE_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    PromoCodeDto actual = promoCodeService.getById(EXIST_PROMO_CODE_ID);

    assertEquals(promoCodeDtoCorrect, actual);
  }

  @Test
  void updatePromoCodeByIdIfNotExist() throws JsonProcessingException {
    promoCodeDtoCorrect.setId(NOT_EXIST_ID);
    given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
        .when()
        .put(PROMO_CODE_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void deletePromoCodeByIdIfExist() {
    given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_PROMO_CODE_ID_WITH_NO_RIDES)
        .when()
        .delete(PROMO_CODE_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    long actualCountPromoCodesAfterDeleting =
        StreamSupport.stream(promoCodeRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY - 1, actualCountPromoCodesAfterDeleting);
  }

  @Test
  void deletePromoCodesByIdIfNotExist() {
    given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .delete(PROMO_CODE_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
    long actualCountDriversAfterDeleting =
        StreamSupport.stream(promoCodeRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY, actualCountDriversAfterDeleting);
  }

  @Test
  void updateDriverByIdIfExistAndIfStartDateAfterFinishDateThanThrowException()
      throws JsonProcessingException {
    promoCodeDtoCorrect.setId(EXIST_PROMO_CODE_ID);
    promoCodeDtoCorrect.setEnd(FINISH_DATE_BEFORE_START_DATE);
    Response actualResponse =
        given()
            .auth()
            .oauth2(adminAccessToken)
            .pathParam("id", EXIST_PROMO_CODE_ID)
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
            .when()
            .put(PROMO_CODE_URL + ID_VARIABLE)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract()
            .response();
    String actual = actualResponse.getBody().asString();
    String expected =
        format(
            FINISH_DATE_EARLY_THAN_START_DATE_EXCEPTION_MESSAGE,
            promoCodeDtoCorrect.getStart(),
            FINISH_DATE_BEFORE_START_DATE);

    assertEquals(expected, actual);
  }

  @Test
  void couldNotGetAllPromocodesWithPassengerRole() {
    given()
        .auth()
        .oauth2(passengerAccessToken)
        .when()
        .get(PROMO_CODE_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void couldNotGetAllPromocodesWithDriverRole() {
    given()
        .auth()
        .oauth2(driverAccessToken)
        .when()
        .get(PROMO_CODE_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }
}
