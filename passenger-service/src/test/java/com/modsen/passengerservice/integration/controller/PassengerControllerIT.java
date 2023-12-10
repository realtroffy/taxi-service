package com.modsen.passengerservice.integration.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.modsen.passengerservice.dto.PassengerAfterRideDto;
import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.integration.testbase.IntegrationTestBase;
import com.modsen.passengerservice.repository.PassengerRepository;
import com.modsen.passengerservice.service.BankCardService;
import com.modsen.passengerservice.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
class PassengerControllerIT extends IntegrationTestBase {

  public static final String PASSENGER_URL = "/api/v1/passengers";
  public static final String ID_VARIABLE = "/{id}";
  public static final String PAGEABLE_PARAMETERS = "?page=0&size=10";
  public static final Long EXIST_ID = 22L;
  public static final Long BANK_CARD_EXIST_ID = 1L;
  public static final Long NOT_EXIST_ID = 35L;
  public static final String NEW_PASSENGER_RATING_VARIABLE = "/{rating}";
  public static final Double NEW_PASSENGER_RATING = 3.0;
  public static final String AFTER_RIDE_URI = "/after-ride";
  public static final String NO_SUCH_PASSENGER_EXCEPTION_MESSAGE =
      "Passenger was not found by id = ";
  public static final int COUNT_EXISTED_ENTITY = 3;
  public static final String EXIST_EMAIL = "john.doe@google.com";
  public static final String FIRST_NAME_LESS_THAN_3_SYMBOLS = "Jo";
  public static final Double INCORRECT_NEW_PASSENGER_RATING = 10.0;

  private final MockMvc mvc;
  private final PassengerRepository passengerRepository;
  private final PassengerService passengerService;
  private final BankCardService bankCardService;
  private ObjectMapper objectMapperWithoutJsonPropertyAccess;
  private ObjectMapper objectMapperWithJsonPropertyAccess;
  private PassengerDto correctPassengerDto;
  private PassengerAfterRideDto passengerAfterRideDto;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    pageable = PageRequest.of(0, 10);

    correctPassengerDto =
        PassengerDto.builder()
            .password("123")
            .firstName("John")
            .lastName("Doe")
            .email("uniq@google.com")
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

    passengerAfterRideDto =
        PassengerAfterRideDto.builder()
            .passengerBankCardId(BANK_CARD_EXIST_ID)
            .rideCost(BigDecimal.TEN)
            .passengerRating(4.0)
            .build();

    objectMapperWithJsonPropertyAccess = new ObjectMapper();
  }

  @Test
  void getPassengerByIdWhenExist() throws Exception {
    mvc.perform(get(PASSENGER_URL + ID_VARIABLE, EXIST_ID))
        .andExpect(jsonPath("$.id").value("22"))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.password").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.email").value("john.doe@google.com"))
        .andExpect(jsonPath("$.bankCards", hasSize(1)))
        .andExpect(status().isOk());
  }

  @Test
  void getPassengerByIdWhenNotExist() throws Exception {
    mvc.perform(get(PASSENGER_URL + ID_VARIABLE, NOT_EXIST_ID))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NoSuchElementException))
        .andExpect(
            result ->
                assertEquals(
                    NO_SUCH_PASSENGER_EXCEPTION_MESSAGE + NOT_EXIST_ID,
                    Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void getAllPassengers() throws Exception {
    mvc.perform(get(PASSENGER_URL + PAGEABLE_PARAMETERS, pageable))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(COUNT_EXISTED_ENTITY)));
  }

  @Test
  void savePassengerIfDtoCorrect() throws Exception {
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(jsonPath("$.id").value("101"))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.password").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.email").value("uniq@google.com"))
        .andExpect(status().isCreated());
  }

  @Test
  void savePassengerIfEmailExist() throws Exception {
    correctPassengerDto.setEmail(EXIST_EMAIL);
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof DataIntegrityViolationException));
  }

  @Test
  void savePassengerIfPasswordLessThan3Symbols() throws Exception {
    correctPassengerDto.setPassword("12");
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void savePassengerIfPasswordMoreThan30Symbols() throws Exception {
    correctPassengerDto.setPassword("1234567891234567891234567912345");
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void savePassengerIfFirstNameLessThan3Symbols() throws Exception {
    correctPassengerDto.setFirstName(FIRST_NAME_LESS_THAN_3_SYMBOLS);
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void savePassengerIfDtoCorrectHasDefaultRating5() throws Exception {
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(jsonPath("$.rating").value("5.0"))
        .andExpect(status().isCreated());
  }

  @Test
  void savePassengerIfEmailIsNull() throws Exception {
    correctPassengerDto.setEmail(null);
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void savePassengerIfFirstNameIsNull() throws Exception {
    correctPassengerDto.setFirstName(null);
    mvc.perform(
            post(PASSENGER_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void deletePassengerByIdIfExist() throws Exception {
    mvc.perform(delete(PASSENGER_URL + ID_VARIABLE, EXIST_ID)).andExpect(status().isNoContent());
    long actualCountPassengersAfterDeleting =
        StreamSupport.stream(passengerRepository.findAll().spliterator(), false).count();

    assertEquals(2, actualCountPassengersAfterDeleting);
  }

  @Test
  void deletePassengerByIdIfPassengerNotExisted() throws Exception {
    mvc.perform(delete(PASSENGER_URL + ID_VARIABLE, NOT_EXIST_ID)).andExpect(status().isNotFound());
    long actualCountPassengersAfterDeleting =
        StreamSupport.stream(passengerRepository.findAll().spliterator(), false).count();

    assertEquals(3, actualCountPassengersAfterDeleting);
  }

  @Test
  void updatePassengerByIdIfNotExist() throws Exception {
    mvc.perform(
            put(PASSENGER_URL + ID_VARIABLE, NOT_EXIST_ID, correctPassengerDto)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NoSuchElementException))
        .andExpect(
            result ->
                assertEquals(
                    NO_SUCH_PASSENGER_EXCEPTION_MESSAGE + NOT_EXIST_ID,
                    Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updatePassengerByIdIfExist() throws Exception {
    mvc.perform(
            put(PASSENGER_URL + ID_VARIABLE, EXIST_ID, correctPassengerDto)
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapperWithoutJsonPropertyAccess.writeValueAsString(correctPassengerDto)))
        .andExpect(status().isNoContent());
    PassengerDto actual = passengerService.getById(EXIST_ID);
    correctPassengerDto.setId(EXIST_ID);

    assertEquals(correctPassengerDto, actual);
  }

  @Test
  void updatePassengerRatingIfExistAndRatingCorrect() throws Exception {
    mvc.perform(
            put(
                PASSENGER_URL + ID_VARIABLE + NEW_PASSENGER_RATING_VARIABLE,
                EXIST_ID,
                NEW_PASSENGER_RATING))
        .andExpect(status().isOk());
    PassengerDto actual = passengerService.getById(EXIST_ID);

    assertEquals(NEW_PASSENGER_RATING, actual.getRating());
  }

  @Test
  void updatePassengerRatingIfExistAndRatingIncorrect() throws Exception {
    mvc.perform(
            put(
                PASSENGER_URL + ID_VARIABLE + NEW_PASSENGER_RATING_VARIABLE,
                EXIST_ID,
                INCORRECT_NEW_PASSENGER_RATING))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof ConstraintViolationException));
  }

  @Test
  void updateAfterRideIfPassengerExistAndPayByBankCard() throws Exception {
    BigDecimal balanceBeforeUpdate =
        bankCardService.getEntityById(passengerAfterRideDto.getPassengerBankCardId()).getBalance();
    mvc.perform(
            put(PASSENGER_URL + AFTER_RIDE_URI + ID_VARIABLE, EXIST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapperWithJsonPropertyAccess.writeValueAsString(passengerAfterRideDto)))
        .andExpect(status().isNoContent());
    PassengerDto actual = passengerService.getById(EXIST_ID);
    BigDecimal actualBalance =
        bankCardService.getEntityById(passengerAfterRideDto.getPassengerBankCardId()).getBalance();

    assertEquals(passengerAfterRideDto.getPassengerRating(), actual.getRating());
    assertEquals(balanceBeforeUpdate.subtract(actualBalance), passengerAfterRideDto.getRideCost());
  }
}
