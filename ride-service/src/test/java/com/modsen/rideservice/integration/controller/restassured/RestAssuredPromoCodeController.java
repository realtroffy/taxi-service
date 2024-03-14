package com.modsen.rideservice.integration.controller.restassured;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.rideservice.dto.PromoCodeDto;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import static io.restassured.RestAssured.given;

@Component
public class RestAssuredPromoCodeController {

  public static final String PROMO_CODE_URL = "/api/v1/promocodes";
  public static final String ID_VARIABLE = "/{id}";
  public static final Long EXIST_PROMO_CODE_ID = 1L;
  public static final Long EXIST_PROMO_CODE_ID_WITH_NO_RIDES = 2L;
  public static final Long NOT_EXIST_ID = 30L;

  public Response getPromoCodeByIdIfExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_PROMO_CODE_ID)
        .when()
        .get(PROMO_CODE_URL + ID_VARIABLE);
  }

  public Response getPromoCodeByIdIfNotExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .get(PROMO_CODE_URL + ID_VARIABLE);
  }

  public Response getAllPromocodes(String adminAccessToken) {
    return given().auth().oauth2(adminAccessToken).when().get(PROMO_CODE_URL);
  }

  public Response savePromoCodeIfDtoCorrect(
      String adminAccessToken, ObjectMapper objectMapper, PromoCodeDto promoCodeDtoCorrect)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
        .when()
        .post(PROMO_CODE_URL);
  }

  public Response updatePromoCodeByIdIfExist(
      String adminAccessToken, ObjectMapper objectMapper, PromoCodeDto promoCodeDtoCorrect)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_PROMO_CODE_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
        .when()
        .put(PROMO_CODE_URL + ID_VARIABLE);
  }

  public Response updatePromoCodeByIdIfNotExist(
      String adminAccessToken, ObjectMapper objectMapper, PromoCodeDto promoCodeDtoCorrect)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
        .when()
        .put(PROMO_CODE_URL + ID_VARIABLE);
  }

  public Response deletePromoCodeByIdIfExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_PROMO_CODE_ID_WITH_NO_RIDES)
        .when()
        .delete(PROMO_CODE_URL + ID_VARIABLE);
  }

  public Response deletePromoCodesByIdIfNotExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .delete(PROMO_CODE_URL + ID_VARIABLE);
  }

  public Response updatePromoCodeByIdIfExistAndIfStartDateAfterFinishDateThanThrowException(
      String adminAccessToken, ObjectMapper objectMapper, PromoCodeDto promoCodeDtoCorrect)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_PROMO_CODE_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(promoCodeDtoCorrect))
        .when()
        .put(PROMO_CODE_URL + ID_VARIABLE);
  }

  public Response couldNotGetAllPromocodesWithPassengerRole(String passengerAccessToken) {
    return given().auth().oauth2(passengerAccessToken).when().get(PROMO_CODE_URL);
  }

  public Response couldNotGetAllPromocodesWithDriverRole(String driverAccessToken) {
    return given().auth().oauth2(driverAccessToken).when().get(PROMO_CODE_URL);
  }
}
