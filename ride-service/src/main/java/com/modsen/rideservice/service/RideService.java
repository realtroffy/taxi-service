package com.modsen.rideservice.service;

import com.modsen.rideservice.dto.PassengerRatingFinishDto;
import com.modsen.rideservice.dto.RideDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RideService {

  RideDto order(RideDto rideDto);

  void finishByDriver(Long rideId, PassengerRatingFinishDto passengerRatingFinishDto);

  RideDto getById(long id);

  List<RideDto> getAll(Pageable pageable);

  void deleteById(long id);

  void update(long id, RideDto rideDto);

  RideDto cancelByPassenger(Long rideId);

  void updateDriverRatingAfterRide(Long rideId, Integer driverRating);
}

