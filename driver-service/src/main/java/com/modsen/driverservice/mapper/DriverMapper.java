package com.modsen.driverservice.mapper;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.model.Driver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {


  Driver toEntity(DriverDto driverDto);

  DriverDto toDto(Driver driver);
}
