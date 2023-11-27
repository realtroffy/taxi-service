package com.modsen.passengerservice.service.impl;

import com.modsen.passengerservice.dto.PassengerAfterRideDto;
import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.mapper.BankCardMapper;
import com.modsen.passengerservice.mapper.PassengerMapper;
import com.modsen.passengerservice.model.BankCard;
import com.modsen.passengerservice.model.Passenger;
import com.modsen.passengerservice.repository.PassengerRepository;
import com.modsen.passengerservice.service.BankCardService;
import com.modsen.passengerservice.service.PassengerService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PassengerServiceImpl implements PassengerService {

  private static final String NO_SUCH_PASSENGER_EXCEPTION_MESSAGE =
      "Passenger was not found by id = ";
  private static final Double DEFAULT_RATING_NEW_PASSENGER = 5.0;
  private final PassengerRepository passengerRepository;
  private final BankCardService bankCardService;
  private final BankCardMapper bankCardMapper;
  private final PassengerMapper passengerMapper;

  @Override
  @Transactional(readOnly = true)
  public PassengerDto getById(long id) {
    Passenger passenger = getPassenger(id);
    return passengerMapper.toDto(passenger);
  }

  private Passenger getPassenger(long id) {
    return passengerRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException(NO_SUCH_PASSENGER_EXCEPTION_MESSAGE + id));
  }

  @Override
  public List<PassengerDto> getAll(Pageable pageable) {
    List<Long> allIds =
        passengerRepository.findAllIds(pageable).getContent().stream()
            .map(BigInteger::longValue)
            .collect(Collectors.toList());
    List<PassengerDto> passengers = new ArrayList<>();
    List<Passenger> byIdIn = passengerRepository.findByIdIn(allIds);
    byIdIn.forEach(passenger -> passengers.add(passengerMapper.toDto(passenger)));
    return passengers;
  }

  @Override
  @Transactional
  public PassengerDto save(PassengerDto passengerDto) {
    Passenger passenger = passengerMapper.toEntity(passengerDto);
    passenger.setRating(DEFAULT_RATING_NEW_PASSENGER);
    Passenger createdPassenger = passengerRepository.save(passenger);
    return passengerMapper.toDto(createdPassenger);
  }

  @Override
  @Transactional
  public void deleteById(long id) {
    passengerRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void update(long id, PassengerDto passengerDto) {
    getPassenger(id);
    passengerDto.setId(id);
    Passenger passenger = passengerMapper.toEntity(passengerDto);
    passengerRepository.save(passenger);
  }

  @Override
  @Transactional
  public PassengerDto updateRating(long id, double rating) {
    Passenger passenger = getPassenger(id);
    passenger.setRating(rating);
    return passengerMapper.toDto(passengerRepository.save(passenger));
  }

  @Override
  @Transactional
  public void addBankCardToPassenger(long passengerId, long bankCardId) {
    Passenger passenger = getPassenger(passengerId);
    BankCard bankCard = bankCardMapper.toEntity(bankCardService.getDtoById(bankCardId));
    passenger.addCard(bankCard);
  }

  @Override
  @Transactional
  public void removeBankCardToPassenger(long passengerId, long bankCardId) {
    Passenger passenger = getPassenger(passengerId);
    BankCard bankCard = bankCardMapper.toEntity(bankCardService.getDtoById(bankCardId));
    passenger.removeBankCard(bankCard);
  }

  @Override
  @Transactional
  public void updateAfterRide(Long passengerId, PassengerAfterRideDto passengerAfterRideDto) {
    Passenger passenger = getPassenger(passengerId);
    passenger.setRating(passengerAfterRideDto.getPassengerRating());
    if (passengerAfterRideDto.getPassengerBankCardId()!=null) {
      BankCard bankCard = bankCardService.getEntityById(passengerAfterRideDto.getPassengerBankCardId());
      bankCard.setBalance(bankCard.getBalance().subtract(passengerAfterRideDto.getRideCost()));
    }
  }
}
