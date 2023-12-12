package com.modsen.passengerservice.service.impl;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.dto.PassengerAfterRideDto;
import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.mapper.BankCardMapper;
import com.modsen.passengerservice.mapper.PassengerMapper;
import com.modsen.passengerservice.model.BankCard;
import com.modsen.passengerservice.model.Passenger;
import com.modsen.passengerservice.repository.PassengerRepository;
import com.modsen.passengerservice.service.BankCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassengerServiceImplTest {

  public static final Long EXIST_ID = 1L;
  public static final Double UPDATED_RATING = 3.0;

  private Passenger passenger;
  private PassengerDto passengerDto;
  private BankCardDto bankCardDto;
  private BankCard bankCard;
  private PassengerAfterRideDto passengerAfterRideDto;
  @Mock private PassengerRepository passengerRepository;
  @Mock private PassengerMapper passengerMapper;
  @Mock private BankCardService bankCardService;
  @Mock private BankCardMapper bankCardMapper;
  @InjectMocks private PassengerServiceImpl passengerService;

  @BeforeEach
  void setUp() {
    passenger = new Passenger();
    passenger.setId(EXIST_ID);
    passengerDto = new PassengerDto();
    passengerDto.setId(EXIST_ID);
    bankCardDto = new BankCardDto();
    bankCardDto.setId(EXIST_ID);
    bankCard = new BankCard();
    bankCard.setId(EXIST_ID);
    bankCard.setBalance(BigDecimal.TEN);
    passengerAfterRideDto = new PassengerAfterRideDto();
    passengerAfterRideDto.setPassengerBankCardId(EXIST_ID);
    passengerAfterRideDto.setRideCost(BigDecimal.ONE);
  }

  @Test
  void getPassengerById() {
    when(passengerRepository.findById(anyLong())).thenReturn(Optional.of(passenger));
    when(passengerMapper.toDto(passenger)).thenReturn(passengerDto);

    PassengerDto actual = passengerService.getById(EXIST_ID);

    assertNotNull(actual);
    assertSame(EXIST_ID, actual.getId());
    verify(passengerRepository).findById(anyLong());
  }

  @Test
  void getAllPassengers() {
    when(passengerRepository.findAllIds(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(BigInteger.ONE)));
    when(passengerRepository.findByIdIn(List.of(1L))).thenReturn(List.of(passenger));
    when(passengerMapper.toDto(passenger)).thenReturn(passengerDto);

    List<PassengerDto> actual = passengerService.getAll(PageRequest.of(0, 10));

    assertNotNull(actual);
    assertTrue(actual.contains(passengerDto));
    assertSame(1, actual.size());
    verify(passengerRepository).findAllIds(PageRequest.of(0, 10));
    verify(passengerRepository).findByIdIn(List.of(1L));
    verify(passengerMapper).toDto(passenger);
  }

  @Test
  void savePassenger() {
    when(passengerMapper.toEntity(passengerDto)).thenReturn(passenger);
    when(passengerRepository.save(passenger)).thenReturn(passenger);
    when(passengerMapper.toDto(passenger)).thenReturn(passengerDto);

    PassengerDto actual = passengerService.save(passengerDto);

    assertEquals(passengerDto, actual);
    verify(passengerMapper).toEntity(passengerDto);
    verify(passengerRepository).save(passenger);
    verify(passengerMapper).toDto(passenger);
  }

  @Test
  void deletePassengerById() {
    doNothing().when(passengerRepository).deleteById(anyLong());

    passengerService.deleteById(anyLong());

    verify(passengerRepository).deleteById(anyLong());
  }

  @Test
  void updatePassenger() {
    when(passengerRepository.findById(anyLong())).thenReturn(Optional.of(passenger));
    when(passengerMapper.toEntity(passengerDto)).thenReturn(passenger);
    when(passengerRepository.save(passenger)).thenReturn(passenger);

    passengerService.update(anyLong(), passengerDto);

    verify(passengerRepository).save(passenger);
    verify(passengerMapper).toEntity(passengerDto);
  }

  @Test
  void updatePassengerRating() {
    when(passengerRepository.findById(anyLong())).thenReturn(Optional.of(passenger));
    when(passengerRepository.save(passenger)).thenReturn(passenger);
    passengerDto.setRating(UPDATED_RATING);
    when(passengerMapper.toDto(passenger)).thenReturn(passengerDto);

    PassengerDto actual = passengerService.updateRating(EXIST_ID, UPDATED_RATING);

    assertNotNull(actual);
    assertEquals(UPDATED_RATING, passengerDto.getRating());
    verify(passengerRepository).findById(anyLong());
    verify(passengerRepository).save(passenger);
    verify(passengerMapper).toDto(passenger);
  }

  @Test
  void addBankCardToPassenger() {
    when(passengerRepository.findById(anyLong())).thenReturn(Optional.of(passenger));
    when(bankCardService.getDtoById(anyLong())).thenReturn(bankCardDto);
    when(bankCardMapper.toEntity(bankCardDto)).thenReturn(bankCard);

    passengerService.addBankCardToPassenger(EXIST_ID, EXIST_ID);

    verify(passengerRepository).findById(anyLong());
    verify(bankCardService).getDtoById(anyLong());
    verify(bankCardMapper).toEntity(bankCardDto);
  }

  @Test
  void removeBankCardToPassenger() {
    when(passengerRepository.findById(anyLong())).thenReturn(Optional.of(passenger));
    when(bankCardService.getDtoById(anyLong())).thenReturn(bankCardDto);
    when(bankCardMapper.toEntity(bankCardDto)).thenReturn(bankCard);

    passengerService.removeBankCardToPassenger(EXIST_ID, EXIST_ID);

    verify(passengerRepository).findById(anyLong());
    verify(bankCardService).getDtoById(anyLong());
    verify(bankCardMapper).toEntity(bankCardDto);
  }

  @Test
  void updateAfterRide() {
    when(passengerRepository.findById(anyLong())).thenReturn(Optional.of(passenger));
    when(bankCardService.getEntityById(anyLong())).thenReturn(bankCard);

    passengerService.updateAfterRide(EXIST_ID, passengerAfterRideDto);

    verify(passengerRepository).findById(anyLong());
    verify(bankCardService).getEntityById(anyLong());
  }
}
