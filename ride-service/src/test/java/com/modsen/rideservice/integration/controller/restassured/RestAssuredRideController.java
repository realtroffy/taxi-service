package com.modsen.rideservice.integration.controller.restassured;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.rideservice.dto.PassengerRatingFinishDto;
import com.modsen.rideservice.dto.RideDto;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import static io.restassured.RestAssured.given;

@Component
public class RestAssuredRideController {

  public static final String RIDE_URL = "/api/v1/rides";
  public static final String ID_VARIABLE = "/{id}";
  public static final String DRIVER_RATING_URL = "/{driverRating}";
  public static final String CANCEL_RIDE_BY_PASSENGER_URL = "/cancel";
  public static final String FINISH_RIDE_BY_DRIVER_URL = "/finish";
  public static final Long EXIST_RIDE_ID = 1L;
  public static final Long EXIST_SECOND_RIDE_ID = 2L;
  public static final Long NOT_EXIST_ID = 30L;
  public static final Integer RATING_AFTER_RIDE = 3;

  public Response getRideIdIfExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_RIDE_ID)
        .when()
        .get(RIDE_URL + ID_VARIABLE);
  }

  public Response getRideByIdIfNotExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .get(RIDE_URL + ID_VARIABLE);
  }

  public Response getAllRides(String adminAccessToken) {
    return given().auth().oauth2(adminAccessToken).when().get(RIDE_URL);
  }

  public Response deleteRideByIdIfExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_RIDE_ID)
        .when()
        .delete(RIDE_URL + ID_VARIABLE);
  }

  public Response deleteRideByIdIfNotExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .delete(RIDE_URL + ID_VARIABLE);
  }

  public Response updateRideByIdIfExist(
      String adminAccessToken, RideDto rideDtoCorrect, ObjectMapper objectMapper)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_RIDE_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(rideDtoCorrect))
        .when()
        .put(RIDE_URL + ID_VARIABLE);
  }

  public Response updateRideByIdIfNotExist(
      String adminAccessToken, RideDto rideDtoCorrect, ObjectMapper objectMapper)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(rideDtoCorrect))
        .when()
        .put(RIDE_URL + ID_VARIABLE);
  }

  public Response updateDriverRatingAfterFinishRideIfExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_RIDE_ID)
        .pathParam("driverRating", RATING_AFTER_RIDE)
        .when()
        .put(RIDE_URL + ID_VARIABLE + DRIVER_RATING_URL);
  }

  public Response updateDriverRatingAfterFinishRideIfNotExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .pathParam("driverRating", RATING_AFTER_RIDE)
        .when()
        .put(RIDE_URL + ID_VARIABLE + DRIVER_RATING_URL);
  }

  public Response cancelByPassengerIfRideExistAndStatusPending(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_SECOND_RIDE_ID)
        .when()
        .put(RIDE_URL + ID_VARIABLE + CANCEL_RIDE_BY_PASSENGER_URL);
  }

  public Response cancelByPassengerIfRideNotExist(String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .put(RIDE_URL + ID_VARIABLE + CANCEL_RIDE_BY_PASSENGER_URL);
  }

  public Response cancelByPassengerIfRideExistAndStatusActiveThenReturnStatusBadRequest(
      String adminAccessToken) {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_SECOND_RIDE_ID)
        .when()
        .put(RIDE_URL + ID_VARIABLE);
  }

  public Response finishByDriverIfRideExist(
      String adminAccessToken,
      ObjectMapper objectMapper,
      PassengerRatingFinishDto passengerRatingFinishDto)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", EXIST_RIDE_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(passengerRatingFinishDto))
        .when()
        .put(RIDE_URL + ID_VARIABLE + FINISH_RIDE_BY_DRIVER_URL);
  }

  public Response finishByDriverIfRideNotExist(
      String adminAccessToken,
      ObjectMapper objectMapper,
      PassengerRatingFinishDto passengerRatingFinishDto)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .pathParam("id", NOT_EXIST_ID)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(passengerRatingFinishDto))
        .when()
        .put(RIDE_URL + ID_VARIABLE + FINISH_RIDE_BY_DRIVER_URL);
  }

  public Response orderRide(
      String adminAccessToken, ObjectMapper objectMapper, RideDto rideDtoCorrect)
      throws JsonProcessingException {
    return given()
        .auth()
        .oauth2(adminAccessToken)
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(rideDtoCorrect))
        .when()
        .post(RIDE_URL);
  }

  public Response orderRideWithoutAccessToken(ObjectMapper objectMapper, RideDto rideDtoCorrect)
      throws JsonProcessingException {
    return given()
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(rideDtoCorrect))
        .when()
        .post(RIDE_URL);
  }
}
