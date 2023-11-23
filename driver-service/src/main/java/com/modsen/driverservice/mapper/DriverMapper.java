package com.modsen.driverservice.mapper;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CarMapper.class)
public interface DriverMapper {

  @Mapping(source = "carDto", target = "car")
  Driver toEntity(DriverDto driverDto);

  @Mapping(source = "car", target = "carDto")
  DriverDto toDto(Driver driver);
}
