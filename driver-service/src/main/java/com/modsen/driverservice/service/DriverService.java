package com.modsen.driverservice.service;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.model.Driver;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DriverService {

    DriverDto getById(long id);

    Driver getDriverById(long id);

    List<DriverDto> getAll(Pageable pageable);

    DriverDto save(DriverDto driverDto);

    void deleteById(long id);

    void update(long id, DriverDto driverDto);

    DriverDto updateRating(long id, double rating);

    void addBankCardToDriver(long driverId, long bankCardId);

    void removeBankCardToDriver(long driverId, long bankCardId);
}
