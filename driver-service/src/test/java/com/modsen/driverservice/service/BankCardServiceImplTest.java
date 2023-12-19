package com.modsen.driverservice.service;

import com.modsen.driverservice.dto.BankCardDto;
import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.mapper.BankCardMapper;
import com.modsen.driverservice.model.BankCard;
import com.modsen.driverservice.repository.BankCardRepository;
import com.modsen.driverservice.service.impl.BankCardServiceImpl;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankCardServiceImplTest {

  public static final Long EXIST_ID = 1L;
  public static final Long NOT_EXIST_ID = 100L;

  private BankCard bankCard;
  private BankCardDto bankCardDto;
  private DriverDto driverDto;
  @Mock private BankCardRepository bankCardRepository;
  @Mock private BankCardMapper bankCardMapper;
  @Mock private DriverService driverService;
  @InjectMocks private BankCardServiceImpl bankCardService;

  @BeforeEach
  void setUp() {
    bankCard = new BankCard();
    bankCard.setId(EXIST_ID);
    bankCardDto = new BankCardDto();
    bankCardDto.setId(EXIST_ID);
    bankCardDto.setDriverId(EXIST_ID);
    driverDto = new DriverDto();
    driverDto.setId(EXIST_ID);
  }

  @Test
  void getDtoById() {
    when(bankCardRepository.findById(anyLong())).thenReturn(Optional.of(bankCard));
    when(bankCardMapper.toDto(bankCard)).thenReturn(bankCardDto);

    BankCardDto actual = bankCardService.getById(EXIST_ID);

    assertNotNull(actual);
    verify(bankCardRepository).findById(anyLong());
  }

  @Test
  void getDtoByIdIfNotExistThanThrowNotSuchElementException() {
    when(bankCardRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> bankCardService.getById(NOT_EXIST_ID));
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
    when(driverService.getById(EXIST_ID)).thenReturn(driverDto);
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
    when(bankCardRepository.findById(EXIST_ID)).thenReturn(Optional.of(bankCard));

    bankCardService.deleteById(EXIST_ID);

    verify(bankCardRepository).deleteById(EXIST_ID);
  }

  @Test
  void deleteByIdIfNotExistThrowNoSuchElementException() {
    when(bankCardRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> bankCardService.deleteById(NOT_EXIST_ID));
    verify(bankCardRepository, never()).deleteById(anyLong());
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

  @Test
  void updateIfNotExistThrowNoSuchElementException() {
    when(bankCardRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class, () -> bankCardService.update(NOT_EXIST_ID, bankCardDto));
    verify(bankCardRepository, never()).save(bankCard);
  }
}
