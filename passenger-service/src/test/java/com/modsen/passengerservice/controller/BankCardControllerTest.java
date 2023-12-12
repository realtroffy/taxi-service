package com.modsen.passengerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.service.BankCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BankCardControllerTest {

  public static final String BANK_CARD_URL = "/api/v1/bankcards";
  public static final String ID_VARIABLE = "/{id}";
  public static final String PAGEABLE_PARAMETERS = "?page=0&size=10";
  public static final Long EXIST_ID = 1L;

  private MockMvc mvc;
  private BankCardDto bankCardDto;
  private ObjectMapper objectMapper;
  private Pageable pageable;
  @Mock private BankCardService bankCardService;
  @InjectMocks private BankCardController bankCardController;

  @BeforeEach
  void setUp() {
    mvc =
        MockMvcBuilders.standaloneSetup(bankCardController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    bankCardDto =
        BankCardDto.builder()
            .id(1L)
            .cardNumber("1234567812345678")
            .passengerId(1L)
            .balance(BigDecimal.TEN)
            .isDefault(true)
            .build();

    objectMapper = new ObjectMapper();

    pageable = PageRequest.of(0,10);
  }

  @Test
  void getBankCardById() throws Exception {
    when(bankCardService.getDtoById(anyLong())).thenReturn(bankCardDto);

    String actual =
        mvc.perform(get(BANK_CARD_URL + ID_VARIABLE, EXIST_ID))
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.cardNumber").value("1234567812345678"))
            .andExpect(jsonPath("$.passengerId").value("1"))
            .andExpect(jsonPath("$.balance").value("10"))
            .andExpect(jsonPath("$.isDefault").value("true"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String expected = objectMapper.writeValueAsString(bankCardDto);

    assertEquals(expected, actual);
    verify(bankCardService).getDtoById(anyLong());
  }

  @Test
  void getAllBankCards() throws Exception {
    when(bankCardService.getAll(any(Pageable.class))).thenReturn(List.of(bankCardDto));

    String actual =
        mvc.perform(get(BANK_CARD_URL + PAGEABLE_PARAMETERS, pageable))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String expected = objectMapper.writeValueAsString(List.of(bankCardDto));

    assertEquals(expected, actual);
    verify(bankCardService).getAll(any(Pageable.class));
  }

  @Test
  void saveBankCard() throws Exception {
    when(bankCardService.save(bankCardDto)).thenReturn(bankCardDto);

    String actual =
        mvc.perform(
                post(BANK_CARD_URL)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankCardDto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String expected = objectMapper.writeValueAsString(bankCardDto);

    assertEquals(expected, actual);
    verify(bankCardService).save(bankCardDto);
  }

  @Test
  void updateBankCardById() throws Exception {
    doNothing().when(bankCardService).update(anyLong(), any(BankCardDto.class));

    mvc.perform(
            put(BANK_CARD_URL + ID_VARIABLE, EXIST_ID, bankCardDto)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bankCardDto)))
        .andExpect(status().isNoContent());

    verify(bankCardService).update(anyLong(), any(BankCardDto.class));
  }

  @Test
  void deleteBankCardById() throws Exception {
    doNothing().when(bankCardService).deleteById(anyLong());

    mvc.perform(delete(BANK_CARD_URL + ID_VARIABLE, EXIST_ID))
        .andExpect(status().isNoContent());

    verify(bankCardService).deleteById(anyLong());
  }
}
