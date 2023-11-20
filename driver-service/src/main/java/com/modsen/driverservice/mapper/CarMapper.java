package com.modsen.driverservice.mapper;

import com.modsen.driverservice.dto.CarDto;
import com.modsen.driverservice.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarMapper {

  @Mapping(source = "driver.id", target = "driverId")
  CarDto toDto(Car car);

  @Mapping(source = "driverId", target = "driver.id")
  Car toEntity(CarDto carDto);
}
