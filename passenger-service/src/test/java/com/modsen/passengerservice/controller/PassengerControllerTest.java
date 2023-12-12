package com.modsen.passengerservice.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.dto.PassengerAfterRideDto;
import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.service.PassengerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PassengerControllerTest {

  public static final String PASSENGER_URL = "/api/v1/passengers";
  public static final String ID_VARIABLE = "/{id}";
  public static final String PASSENGER_ID_VARIABLE = "/{passengerId}";
  public static final String BANK_CARD_ID_VARIABLE = "/{bankCardId}";
  public static final String PAGEABLE_PARAMETERS = "?page=0&size=10";
  public static final Long EXIST_ID = 1L;
  public static final String NEW_PASSENGER_RATING_VARIABLE = "/{rating}";
  public static final Double NEW_PASSENGER_RATING = 3.0;
  public static final String AFTER_RIDE_URI = "/after-ride";
  public static final String BANK_CARD_URI = "/bankcards";

  private MockMvc mvc;
  private PassengerDto passengerDto;
  private PassengerDto passengerDtoWithUpdatedRating;
  private ObjectMapper objectMapperWithoutJsonPropertyAccess;
  private ObjectMapper objectMapperWithJsonPropertyAccess;
  private PassengerAfterRideDto passengerAfterRideDto;
  private Pageable pageable;

  @Mock private PassengerService passengerService;
  @InjectMocks private PassengerController passengerController;

  @BeforeEach
  void setUp() {
    mvc =
        MockMvcBuilders.standaloneSetup(passengerController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    BankCardDto bankCardDto =
        BankCardDto.builder()
            .id(1L)
            .cardNumber("1234567812345678")
            .passengerId(1L)
            .balance(BigDecimal.TEN)
            .isDefault(true)
            .build();

    passengerDto =
        PassengerDto.builder()
            .id(1L)
            .password("123")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@google.com")
            .bankCards(List.of(bankCardDto))
            .build();

    passengerDtoWithUpdatedRating = passengerDto;
    passengerDtoWithUpdatedRating.setRating(NEW_PASSENGER_RATING);

    objectMapperWithoutJsonPropertyAccess =
        new ObjectMapper()
            .setAnnotationIntrospector(
                new JacksonAnnotationIntrospector() {
                  @Override
                  public JsonProperty.Access findPropertyAccess(Annotated m) {
                    return null;
                  }
                });

    objectMapperWithJsonPropertyAccess = new ObjectMapper();

    passengerAfterRideDto =
        PassengerAfterRideDto.builder().rideCost(BigDecimal.TEN).passengerRating(4.0).build();

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void getPassengerById() throws Exception {
    when(passengerService.getById(anyLong())).thenReturn(passengerDto);

    String actual =
        mvc.perform(get(PASSENGER_URL + ID_VARIABLE, EXIST_ID))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.password").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.email").value("john.doe@google.com"))
            .andExpect(jsonPath("$.bankCards", hasSize(1)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String expected = objectMapperWithJsonPropertyAccess.writeValueAsString(passengerDto);

    assertEquals(expected, actual);
    verify(passengerService).getById(anyLong());
  }

  @Test
  void getAllPassengers() throws Exception {
    when(passengerService.getAll(any(Pageable.class))).thenReturn(List.of(passengerDto));

    mvc.perform(get(PASSENGER_URL + PAGEABLE_PARAMETERS, pageable)).andExpect(status().isOk());

    verify(passengerService).getAll(any(Pageable.class));
  }

  @Test
  void savePassenger() throws Exception {
    when(passengerService.save(passengerDto)).thenReturn(passengerDto);

    String actual =
        mvc.perform(
                post(PASSENGER_URL)
                    .contentType(APPLICATION_JSON)
                    .content(
                        objectMapperWithoutJsonPropertyAccess.writeValueAsString(passengerDto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String expected = objectMapperWithJsonPropertyAccess.writeValueAsString(passengerDto);

    assertEquals(expected, actual);
    verify(passengerService).save(passengerDto);
  }

  @Test
  void deletePassengerById() throws Exception {
    doNothing().when(passengerService).deleteById(anyLong());

    mvc.perform(delete(PASSENGER_URL + ID_VARIABLE, EXIST_ID)).andExpect(status().isNoContent());

    verify(passengerService).deleteById(anyLong());
  }

  @Test
  void updatePassengerByID() throws Exception {
    doNothing().when(passengerService).update(anyLong(), any(PassengerDto.class));

    mvc.perform(
            put(PASSENGER_URL + ID_VARIABLE, EXIST_ID, passengerDto)
                .contentType(APPLICATION_JSON)
                .content(objectMapperWithoutJsonPropertyAccess.writeValueAsString(passengerDto)))
        .andExpect(status().isNoContent());

    verify(passengerService).update(anyLong(), any(PassengerDto.class));
  }

  @Test
  void updatePassengerRating() throws Exception {
    when(passengerService.updateRating(anyLong(), anyDouble()))
        .thenReturn(passengerDtoWithUpdatedRating);

    String actual =
        mvc.perform(
                put(
                    PASSENGER_URL + ID_VARIABLE + NEW_PASSENGER_RATING_VARIABLE,
                    EXIST_ID,
                    NEW_PASSENGER_RATING))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String expected =
        objectMapperWithJsonPropertyAccess.writeValueAsString(passengerDtoWithUpdatedRating);

    assertEquals(expected, actual);
    verify(passengerService).updateRating(anyLong(), anyDouble());
  }

  @Test
  void addBankCardToPassenger() throws Exception {
    doNothing().when(passengerService).addBankCardToPassenger(anyLong(), anyLong());

    mvc.perform(
            put(
                PASSENGER_URL + PASSENGER_ID_VARIABLE + BANK_CARD_URI + BANK_CARD_ID_VARIABLE,
                EXIST_ID,
                EXIST_ID))
        .andExpect(status().isNoContent());

    verify(passengerService).addBankCardToPassenger(anyLong(), anyLong());
  }

  @Test
  void deleteBankCardFromPassenger() throws Exception {
    doNothing().when(passengerService).removeBankCardToPassenger(anyLong(), anyLong());

    mvc.perform(
            delete(
                PASSENGER_URL + PASSENGER_ID_VARIABLE + BANK_CARD_URI + BANK_CARD_ID_VARIABLE,
                EXIST_ID,
                EXIST_ID))
        .andExpect(status().isNoContent());

    verify(passengerService).removeBankCardToPassenger(anyLong(), anyLong());
  }

  @Test
  void updateAfterRide() throws Exception {
    doNothing().when(passengerService).updateAfterRide(anyLong(), any(PassengerAfterRideDto.class));

    mvc.perform(
            put(PASSENGER_URL + AFTER_RIDE_URI + ID_VARIABLE, EXIST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapperWithJsonPropertyAccess.writeValueAsString(passengerAfterRideDto)))
        .andExpect(status().isNoContent());

    verify(passengerService).updateAfterRide(anyLong(), any(PassengerAfterRideDto.class));
  }
}
