package com.modsen.driverservice.service;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.dto.DriverRatingDto;
import com.modsen.driverservice.model.Driver;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DriverService {

    DriverDto getById(long id);

    Driver getDriverById(long id);

    List<DriverDto> getAll(Pageable pageable);

    DriverDto save(DriverDto driverDto);

    void deleteById(long id);

    void update(long id, DriverDto driverDto);

    DriverDto updateRating(long id, DriverRatingDto driverRatingDto);

    void addBankCardToDriver(long driverId, long bankCardId);

    void removeBankCardToDriver(long driverId, long bankCardId);

    DriverDto updateAvailabilityToTrueAfterFinishedRide(long driverId);

    List<DriverDto> getDriversByIds(List<Long> listId);

    void getAvailableRandomDriverIfExistAndChangeAvailabilityToFalse(StreamsBuilder kStreamBuilder);
}
