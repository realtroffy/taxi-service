package com.modsen.driverservice.service.impl;

import com.modsen.driverservice.dto.CarDto;
import com.modsen.driverservice.mapper.CarMapper;
import com.modsen.driverservice.model.Car;
import com.modsen.driverservice.model.Driver;
import com.modsen.driverservice.repository.CarRepository;
import com.modsen.driverservice.service.CarService;
import com.modsen.driverservice.service.DriverService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class CarServiceImpl implements CarService {

    private static final String NO_SUCH_CAR_EXCEPTION_MESSAGE = "Car was not found by id = ";
    private final CarMapper carMapper;
    private final CarRepository carRepository;
    private final DriverService driverService;

    @Override
    @Transactional(readOnly = true)
    public CarDto getById(long id) {
        Car car = getCar(id);
        return carMapper.toDto(car);
    }

    private Car getCar(long id) {
        return carRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(NO_SUCH_CAR_EXCEPTION_MESSAGE + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarDto> getAll(Pageable pageable) {
        List<CarDto> cars = new ArrayList<>();
        carRepository.findAll(pageable).forEach(car -> cars.add(carMapper.toDto(car)));
        return cars;
    }

    @Override
    @Transactional
    public CarDto save(CarDto carDto) {
        Car car = carMapper.toEntity(carDto);
        Driver driver = driverService.getDriverById(carDto.getDriverId());
        driver.setCar(car);
        car.setDriver(driver);
        Car createdCar = carRepository.save(car);
        return carMapper.toDto(createdCar);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        carRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void update(CarDto carDto) {
        getCar(carDto.getDriverId());
        Car car = carMapper.toEntity(carDto);
        carRepository.save(car);
    }
}
