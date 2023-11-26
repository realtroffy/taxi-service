package com.modsen.passengerservice.service;

import com.modsen.passengerservice.dto.PassengerAfterRideDto;
import com.modsen.passengerservice.dto.PassengerDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PassengerService {

  PassengerDto getById(long id);

  List<PassengerDto> getAll(Pageable pageable);

  PassengerDto save(PassengerDto passengerDto);

  void deleteById(long id);

  void update(long id, PassengerDto passengerDto);

  PassengerDto updateRating(long id, double rating);

  void addBankCardToPassenger(long passengerId, long bankCardId);

  void removeBankCardToPassenger(long passengerId, long bankCardId);

  void updateAfterRide(Long passengerId, PassengerAfterRideDto passengerAfterRideDto);
}
