package com.modsen.passengerservice.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.integration.testbase.IntegrationTestBase;
import com.modsen.passengerservice.repository.BankCardRepository;
import com.modsen.passengerservice.service.BankCardService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
class BankCardControllerIT extends IntegrationTestBase {

  public static final String BANK_CARD_URL = "/api/v1/bankcards";
  public static final String ID_VARIABLE = "/{id}";
  public static final String PAGEABLE_PARAMETERS = "?page=0&size=10";
  public static final Long EXIST_ID = 1L;
  public static final Long NOT_EXIST_ID = 30L;
  public static final int COUNT_EXISTED_ENTITY = 3;
  public static final String NO_SUCH_BANK_CARD_EXCEPTION_MESSAGE =
      "Bank card was not found by id = ";
  public static final String NO_SUCH_PASSENGER_EXCEPTION_MESSAGE =
      "Passenger was not found by id = ";
  public static final String CARD_NUMBER_LESS_THAN_16_DIGITS = "1111";
  public static final String CARD_NUMBER_WITH_NOT_ONLY_DIGITS = "111111111111111a";
  public static final String EXISTED_BANK_CARD_NUMBER = "1234567891011131";

  private final MockMvc mvc;
  private final ObjectMapper objectMapper;
  private final BankCardService bankCardService;
  private final BankCardRepository bankCardRepository;
  private Pageable pageable;
  private BankCardDto bankCardDtoCorrect;

  @BeforeEach
  void setUp() {
    pageable = PageRequest.of(0, 10);
    bankCardDtoCorrect =
        BankCardDto.builder()
            .cardNumber("1234567812345678")
            .passengerId(22L)
            .balance(BigDecimal.TEN)
            .isDefault(false)
            .build();
  }

  @Test
  void getBankCardByIdIfExist() throws Exception {
    mvc.perform(get(BANK_CARD_URL + ID_VARIABLE, EXIST_ID))
        .andExpect(jsonPath("$.id").value("1"))
        .andExpect(jsonPath("$.cardNumber").value("1234567891011131"))
        .andExpect(jsonPath("$.passengerId").value("22"))
        .andExpect(jsonPath("$.balance").value("100"))
        .andExpect(jsonPath("$.isDefault").value("true"))
        .andExpect(status().isOk());
  }

  @Test
  void getBankCardByIdIfNotExist() throws Exception {
    mvc.perform(get(BANK_CARD_URL + ID_VARIABLE, NOT_EXIST_ID))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NoSuchElementException))
        .andExpect(
            result ->
                assertEquals(
                    NO_SUCH_BANK_CARD_EXCEPTION_MESSAGE + NOT_EXIST_ID,
                    Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void getAllBankCards() throws Exception {
    mvc.perform(get(BANK_CARD_URL + PAGEABLE_PARAMETERS, pageable))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(COUNT_EXISTED_ENTITY)));
  }

  @Test
  void saveBankCardIfDtoCorrect() throws Exception {
    mvc.perform(
            post(BANK_CARD_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(jsonPath("$.id").value("4"))
        .andExpect(jsonPath("$.cardNumber").value("1234567812345678"))
        .andExpect(jsonPath("$.passengerId").value("22"))
        .andExpect(jsonPath("$.balance").value("10"))
        .andExpect(jsonPath("$.isDefault").value("false"))
        .andExpect(status().isCreated());
  }

  @Test
  void saveBankCardIfDtoWithNotExistedPassengerId() throws Exception {
    bankCardDtoCorrect.setPassengerId(NOT_EXIST_ID);
    mvc.perform(
            post(BANK_CARD_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
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
  void saveBankCardIfDtoWithNegativeBalance() throws Exception {
    bankCardDtoCorrect.setBalance(new BigDecimal(-10));
    mvc.perform(
            post(BANK_CARD_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void saveBankCardIfDtoIfCardNumberHasNot16Digits() throws Exception {
    bankCardDtoCorrect.setCardNumber(CARD_NUMBER_LESS_THAN_16_DIGITS);
    mvc.perform(
            post(BANK_CARD_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void saveBankCardIfDtoIfCardNumberHasNotOnlyDigits() throws Exception {
    bankCardDtoCorrect.setCardNumber(CARD_NUMBER_WITH_NOT_ONLY_DIGITS);
    mvc.perform(
            post(BANK_CARD_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof MethodArgumentNotValidException));
  }

  @Test
  void saveBankCardIfBankCardNumberAlreadyExist() throws Exception {
    bankCardDtoCorrect.setCardNumber(EXISTED_BANK_CARD_NUMBER);
    mvc.perform(
            post(BANK_CARD_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof DataIntegrityViolationException));
  }

  @Test
  void saveBankCardIfAlreadyExistBankCardWithIsDefaultTrue() throws Exception {
    bankCardDtoCorrect.setIsDefault(true);
    mvc.perform(
            post(BANK_CARD_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof DataIntegrityViolationException));
  }

  @Test
  void updateBankCardByIdIfExist() throws Exception {
    mvc.perform(
            put(BANK_CARD_URL + ID_VARIABLE, EXIST_ID, bankCardDtoCorrect)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(status().isNoContent());
    BankCardDto actual = bankCardService.getDtoById(EXIST_ID);
    bankCardDtoCorrect.setId(EXIST_ID);

    assertEquals(bankCardDtoCorrect, actual);
  }

  @Test
  void updateBankCardByIdIfNotExist() throws Exception {
    mvc.perform(
            put(BANK_CARD_URL + ID_VARIABLE, NOT_EXIST_ID, bankCardDtoCorrect)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDtoCorrect)))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NoSuchElementException))
        .andExpect(
            result ->
                assertEquals(
                    NO_SUCH_BANK_CARD_EXCEPTION_MESSAGE + NOT_EXIST_ID,
                    Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void deleteBankCardByIdIfBankCardExisted() throws Exception {
    mvc.perform(delete(BANK_CARD_URL + ID_VARIABLE, EXIST_ID)).andExpect(status().isNoContent());
    long actualCountBankCardsAfterDeleting =
        StreamSupport.stream(bankCardRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY - 1, actualCountBankCardsAfterDeleting);
  }

  @Test
  void deleteBankCardByIdIfBankCardNotExisted() throws Exception {
    mvc.perform(delete(BANK_CARD_URL + ID_VARIABLE, NOT_EXIST_ID)).andExpect(status().isNotFound());
    long actualCountBankCardsAfterFailDeleting =
        StreamSupport.stream(bankCardRepository.findAll().spliterator(), false).count();

    assertEquals(COUNT_EXISTED_ENTITY, actualCountBankCardsAfterFailDeleting);
  }
}
