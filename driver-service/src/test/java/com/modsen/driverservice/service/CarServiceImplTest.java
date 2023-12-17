package com.modsen.driverservice.service;

import com.modsen.driverservice.dto.CarDto;
import com.modsen.driverservice.mapper.CarMapper;
import com.modsen.driverservice.model.Car;
import com.modsen.driverservice.model.Driver;
import com.modsen.driverservice.repository.CarRepository;
import com.modsen.driverservice.service.impl.CarServiceImpl;
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
class CarServiceImplTest {

  public static final Long EXIST_ID = 1L;

  private Car car;
  private CarDto carDto;
  private Driver driver;
  @Mock private CarRepository carRepository;
  @Mock private DriverService driverService;
  @Mock private CarMapper carMapper;
  @InjectMocks private CarServiceImpl carService;

  @BeforeEach
  void setUp() {
    car = new Car();
    car.setId(EXIST_ID);
    carDto = new CarDto();
    carDto.setDriverId(EXIST_ID);
    driver = new Driver();
    driver.setId(EXIST_ID);
  }

  @Test
  void getDtoById() {
    when(carRepository.findById(anyLong())).thenReturn(Optional.of(car));
    when(carMapper.toDto(car)).thenReturn(carDto);

    CarDto actual = carService.getById(EXIST_ID);

    assertNotNull(actual);
    assertSame(EXIST_ID, actual.getDriverId());
    verify(carRepository).findById(anyLong());
  }

  @Test
  void getAll() {
    when(carRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(car)));
    when(carMapper.toDto(car)).thenReturn(carDto);

    List<CarDto> actual = carService.getAll(PageRequest.of(0, 10));

    assertNotNull(actual);
    assertSame(1, actual.size());
    verify(carRepository).findAll(any(Pageable.class));
    verify(carMapper).toDto(car);
  }

  @Test
  void save() {
    when(carMapper.toEntity(carDto)).thenReturn(car);
    when(driverService.getDriverById(EXIST_ID)).thenReturn(driver);
    when(carRepository.save(car)).thenReturn(car);
    when(carMapper.toDto(car)).thenReturn(carDto);

    CarDto actual = carService.save(carDto);

    assertEquals(carDto, actual);
    verify(carMapper).toEntity(carDto);
    verify(driverService).getDriverById(EXIST_ID);
    verify(carRepository).save(car);
    verify(carMapper).toDto(car);
  }

  @Test
  void deleteById() {
    doNothing().when(carRepository).deleteById(anyLong());

    carService.deleteById(EXIST_ID);

    verify(carRepository).deleteById(anyLong());
  }

  @Test
  void update() {
    when(carRepository.findById(anyLong())).thenReturn(Optional.of(car));
    when(carMapper.toEntity(carDto)).thenReturn(car);
    when(carRepository.save(car)).thenReturn(car);

    carService.update(carDto);

    verify(carRepository).save(car);
    verify(carMapper).toEntity(carDto);
  }
}
