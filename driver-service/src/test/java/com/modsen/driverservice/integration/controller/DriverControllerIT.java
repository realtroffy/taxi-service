package com.modsen.driverservice.integration.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.modsen.driverservice.dto.CarDto;
import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.dto.DriverPageDto;
import com.modsen.driverservice.dto.IdPageDto;
import com.modsen.driverservice.integration.testbase.IntegrationTestBase;
import com.modsen.driverservice.model.Driver;
import com.modsen.driverservice.repository.DriverRepository;
import com.modsen.driverservice.service.DriverService;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
class DriverControllerIT extends IntegrationTestBase {

  public static final String DRIVER_URL = "/api/v1/drivers";
  public static final String AVAILABLE_POSTFIX_URL = "/available-true";
  public static final String LIST_ID_POSTFIX_URL = "/list-id";
  public static final String ID_VARIABLE = "/{id}";
  public static final Long EXIST_DRIVER_ID = 22L;
  public static final Long EXIST_DRIVER_ID_WITH_AVAILABILITY_FALSE = 66L;
  public static final Long NOT_EXIST_ID = 30L;
  public static final int COUNT_EXISTED_ENTITY = 3;
  public static final Long GENERATED_ID_AFTER_SAVE = 101L;
  public static final Double DEFAULT_RATING = 5.0;

  private final DriverService driverService;
  private final DriverRepository driverRepository;
  private DriverDto driverDtoCorrect;
  private CarDto carDtoCorrect;
  private IdPageDto idPageDto;
  private ObjectMapper objectMapperWithoutJsonPropertyAccess;

  @BeforeEach
  @Override
  protected void setUp() {
    super.setUp();

    driverDtoCorrect =
        DriverDto.builder()
            .firstName("Adam")
            .lastName("Sandler")
            .email("adam@google.com")
            .password("123")
            .build();

    carDtoCorrect =
        CarDto.builder()
            .driverId(EXIST_DRIVER_ID)
            .colour("green")
            .model("lada vesta")
            .number("1248-AC7")
            .build();

    idPageDto =
        IdPageDto.builder()
            .listId(List.of(EXIST_DRIVER_ID, EXIST_DRIVER_ID_WITH_AVAILABILITY_FALSE))
            .build();

    objectMapperWithoutJsonPropertyAccess =
        new ObjectMapper()
            .setAnnotationIntrospector(
                new JacksonAnnotationIntrospector() {
                  @Override
                  public JsonProperty.Access findPropertyAccess(Annotated m) {
                    return null;
                  }
                });
  }

  @Test
  void getDriverByIdIfExist() {
    given()
        .pathParam("id", EXIST_DRIVER_ID)
        .when()
        .get(DRIVER_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.OK.value())
        .and()
        .body("id", equalTo(EXIST_DRIVER_ID.intValue()))
        .body("$", not(hasKey("password")))
        .body("firstName", equalTo("John"))
        .body("lastName", equalTo("Doe"))
        .body("rating", equalTo(5.0f))
        .body("isAvailable", equalTo(true))
        .body("carDto.driverId", equalTo(EXIST_DRIVER_ID.intValue()))
        .body("carDto.colour", equalTo("green"))
        .body("carDto.model", equalTo("lada vesta"))
        .body("carDto.number", equalTo("1248-AC7"));
  }

  @Test
  void getDriverByIdIfNotExist() {
    given()
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .get(DRIVER_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void getAllDrivers() {
    DriverPageDto actual =
        given()
            .when()
            .get(DRIVER_URL)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(DriverPageDto.class);

    assertEquals(COUNT_EXISTED_ENTITY, actual.getDriverDtoList().size());
  }

  @Test
  void saveDriverIfDtoCorrect() throws JsonProcessingException {
    DriverDto actual =
        given()
            .contentType(ContentType.JSON)
            .body(objectMapperWithoutJsonPropertyAccess.writeValueAsString(driverDtoCorrect))
            .when()
            .post(DRIVER_URL)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(DriverDto.class);
    actual.setPassword(driverDtoCorrect.getPassword());
    DriverDto expected = driverService.getById(GENERATED_ID_AFTER_SAVE);

    assertEquals(expected, actual);
  }

  @Test
  void updateDriverByIdIfNotExist() throws JsonProcessingException {
    driverDtoCorrect.setId(NOT_EXIST_ID);
    driverDtoCorrect.setRating(DEFAULT_RATING);
    given()
        .pathParam("id", NOT_EXIST_ID)
        .contentType(ContentType.JSON)
        .body(objectMapperWithoutJsonPropertyAccess.writeValueAsString(driverDtoCorrect))
        .when()
        .put(DRIVER_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void updateDriverByIdIfExist() throws JsonProcessingException {
    driverDtoCorrect.setId(EXIST_DRIVER_ID);
    driverDtoCorrect.setRating(DEFAULT_RATING);
    given()
        .pathParam("id", EXIST_DRIVER_ID)
        .contentType(ContentType.JSON)
        .body(objectMapperWithoutJsonPropertyAccess.writeValueAsString(driverDtoCorrect))
        .when()
        .put(DRIVER_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    driverDtoCorrect.setCarDto(carDtoCorrect);
    DriverDto actual = driverService.getById(EXIST_DRIVER_ID);

    assertEquals(driverDtoCorrect, actual);
  }

  @Test
  void deleteDriverByIdIfDriverExisted() {
    given()
        .pathParam("id", EXIST_DRIVER_ID)
        .when()
        .delete(DRIVER_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    long actualCountCarsAfterDeleting =
        StreamSupport.stream(driverRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY - 1, actualCountCarsAfterDeleting);
  }

  @Test
  void deleteDriverByIdIfDriverNotExisted() {
    given()
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .delete(DRIVER_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
    long actualCountCarAfterDeleting =
        StreamSupport.stream(driverRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY, actualCountCarAfterDeleting);
  }

  @Test
  void updateDriverAvailabilityIfDriverExist() {
    given()
        .pathParam("id", EXIST_DRIVER_ID_WITH_AVAILABILITY_FALSE)
        .when()
        .put(DRIVER_URL + ID_VARIABLE + AVAILABLE_POSTFIX_URL)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    DriverDto actual = driverService.getById(EXIST_DRIVER_ID_WITH_AVAILABILITY_FALSE);

    assertTrue(actual.getIsAvailable());
  }

  @Test
  void getDriversByIds() {
    DriverPageDto actual =
        given()
            .contentType(ContentType.JSON)
            .body(idPageDto)
            .when()
            .post(DRIVER_URL + LIST_ID_POSTFIX_URL)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(DriverPageDto.class);

    assertEquals(2, actual.getDriverDtoList().size());
  }
}
