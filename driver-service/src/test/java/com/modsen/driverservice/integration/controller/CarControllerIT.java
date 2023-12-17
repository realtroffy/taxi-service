package com.modsen.driverservice.integration.controller;

import com.modsen.driverservice.dto.CarDto;
import com.modsen.driverservice.dto.CarPageDto;
import com.modsen.driverservice.integration.testbase.IntegrationTestBase;
import com.modsen.driverservice.repository.CarRepository;
import com.modsen.driverservice.service.CarService;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
class CarControllerIT extends IntegrationTestBase {

  public static final String CAR_URL = "/api/v1/cars";
  public static final String ID_VARIABLE = "/{id}";
  public static final Long EXIST_DRIVER_AND_CAR_ID = 22L;
  public static final Long NOT_EXIST_ID = 30L;
  public static final int COUNT_EXISTED_ENTITY = 3;
  public static final String EXISTED_CAR_NUMBER = "1248-AC7";

  private final CarService carService;
  private final CarRepository carRepository;
  private CarDto carDtoCorrect;

  @BeforeEach
  @Override
  protected void setUp() {
    super.setUp();

    carDtoCorrect =
        CarDto.builder()
            .driverId(EXIST_DRIVER_AND_CAR_ID)
            .colour("green")
            .model("lada vesta")
            .number("1248-AC7")
            .build();
  }

  @Test
  void getCarByIdIfExist() {
    given()
        .pathParam("id", EXIST_DRIVER_AND_CAR_ID)
        .when()
        .get(CAR_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.OK.value())
        .and()
        .body("driverId", equalTo(EXIST_DRIVER_AND_CAR_ID.intValue()))
        .body("colour", equalTo("green"))
        .body("model", equalTo("lada vesta"))
        .body("number", equalTo("1248-AC7"));
  }

  @Test
  void getCarByIdIfNotExist() {
    given()
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .get(CAR_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void getAllCars() {
    CarPageDto actual =
        given()
            .when()
            .get(CAR_URL)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(CarPageDto.class);

    assertEquals(COUNT_EXISTED_ENTITY, actual.getCarPageDtoList().size());
  }

  @Test
  void saveCarIfDtoCorrect() {
    CarDto actual =
        given()
            .contentType(ContentType.JSON)
            .body(carDtoCorrect)
            .when()
            .post(CAR_URL)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(CarDto.class);
    carDtoCorrect.setDriverId(EXIST_DRIVER_AND_CAR_ID);

    assertEquals(carDtoCorrect, actual);
  }

  @Test
  void saveCarIfDtoWithNotExistedDriverId() {
    carDtoCorrect.setDriverId(NOT_EXIST_ID);
    given()
        .contentType(ContentType.JSON)
        .body(carDtoCorrect)
        .when()
        .post(CAR_URL)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void saveCarIfSuchNumberExistAndAssociatedWithAnotherCar() {
    carDtoCorrect.setDriverId(100L);
    given()
        .contentType(ContentType.JSON)
        .body(carDtoCorrect)
        .when()
        .post(CAR_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void updateCarByIdIfExist() {
    given()
        .contentType(ContentType.JSON)
        .body(carDtoCorrect)
        .when()
        .put(CAR_URL)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    CarDto actual = carService.getById(EXIST_DRIVER_AND_CAR_ID);

    assertEquals(carDtoCorrect, actual);
  }

  @Test
  void updateCarByIdIfNotExist() {
    carDtoCorrect.setDriverId(NOT_EXIST_ID);
    given()
        .contentType(ContentType.JSON)
        .body(carDtoCorrect)
        .when()
        .put(CAR_URL)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void deleteCarByIdIfCarExisted() {
    given()
        .pathParam("id", EXIST_DRIVER_AND_CAR_ID)
        .when()
        .delete(CAR_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());
    long actualCountCarsAfterDeleting =
        StreamSupport.stream(carRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY - 1, actualCountCarsAfterDeleting);
  }

  @Test
  void deleteCarByIdIfCarNotExisted() {
    given()
        .pathParam("id", NOT_EXIST_ID)
        .when()
        .delete(CAR_URL + ID_VARIABLE)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
    long actualCountCarAfterDeleting =
        StreamSupport.stream(carRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY, actualCountCarAfterDeleting);
  }
}
