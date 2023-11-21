package com.modsen.driverservice.service;

import com.modsen.driverservice.dto.CarDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarService {
    CarDto getById(long id);

    List<CarDto> getAll(Pageable pageable);

    CarDto save(CarDto carDto);

    void deleteById(long id);

    void update(CarDto carDto);
}
