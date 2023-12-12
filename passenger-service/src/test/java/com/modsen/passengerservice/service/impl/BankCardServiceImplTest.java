package com.modsen.passengerservice.service.impl;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.mapper.BankCardMapper;
import com.modsen.passengerservice.model.BankCard;
import com.modsen.passengerservice.repository.BankCardRepository;
import com.modsen.passengerservice.service.PassengerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankCardServiceImplTest {

  public static final Long EXIST_ID = 1L;

  private BankCard bankCard;
  private BankCardDto bankCardDto;
  private PassengerDto passengerDto;
  @Mock private BankCardRepository bankCardRepository;
  @Mock private BankCardMapper bankCardMapper;
  @Mock private PassengerService passengerService;
  @InjectMocks private BankCardServiceImpl bankCardService;

  @BeforeEach
  void setUp() {
    bankCard = new BankCard();
    bankCard.setId(EXIST_ID);
    bankCardDto = new BankCardDto();
    bankCardDto.setId(EXIST_ID);
    bankCardDto.setPassengerId(EXIST_ID);
    passengerDto = new PassengerDto();
    passengerDto.setId(EXIST_ID);
    passengerDto.setBankCards(List.of(bankCardDto));
  }

  @Test
  void getDtoById() {
    when(bankCardRepository.findById(anyLong())).thenReturn(Optional.of(bankCard));
    when(bankCardMapper.toDto(bankCard)).thenReturn(bankCardDto);

    BankCardDto actual = bankCardService.getDtoById(EXIST_ID);

    assertNotNull(actual);
    assertSame(EXIST_ID, actual.getId());
    verify(bankCardRepository).findById(anyLong());
  }

  @Test
  void getEntityById() {
    when(bankCardRepository.findById(anyLong())).thenReturn(Optional.of(bankCard));

    BankCard actual = bankCardService.getEntityById(anyLong());

    assertNotNull(actual);
    assertSame(EXIST_ID, actual.getId());
    verify(bankCardRepository).findById(anyLong());
  }

  @Test
  void getAll() {
    when(bankCardRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(bankCard)));
    when(bankCardMapper.toDto(bankCard)).thenReturn(bankCardDto);

    List<BankCardDto> actual = bankCardService.getAll(PageRequest.of(0, 10));

    assertNotNull(actual);
    assertSame(1, actual.size());
    verify(bankCardRepository).findAll(any(Pageable.class));
    verify(bankCardMapper).toDto(bankCard);
  }

  @Test
  void save() {
    when(passengerService.getById(EXIST_ID)).thenReturn(passengerDto);
    when(bankCardMapper.toEntity(bankCardDto)).thenReturn(bankCard);
    when(bankCardRepository.save(bankCard)).thenReturn(bankCard);
    when(bankCardMapper.toDto(bankCard)).thenReturn(bankCardDto);

    BankCardDto actual = bankCardService.save(bankCardDto);

    assertEquals(bankCardDto, actual);
    verify(bankCardMapper).toEntity(bankCardDto);
    verify(bankCardRepository).save(bankCard);
    verify(bankCardMapper).toDto(bankCard);
  }

  @Test
  void deleteById() {
    doNothing().when(bankCardRepository).deleteById(anyLong());

    bankCardService.deleteById(EXIST_ID);

    verify(bankCardRepository).deleteById(anyLong());
  }

  @Test
  void update() {
    when(bankCardRepository.findById(anyLong())).thenReturn(Optional.of(bankCard));
    when(bankCardMapper.toEntity(bankCardDto)).thenReturn(bankCard);
    when(bankCardRepository.save(bankCard)).thenReturn(bankCard);

    bankCardService.update(EXIST_ID, bankCardDto);

    verify(bankCardRepository).save(bankCard);
    verify(bankCardMapper).toEntity(bankCardDto);
  }
}
