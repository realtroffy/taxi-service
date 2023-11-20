package com.modsen.driverservice.mapper;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BankCardMapper.class, CarMapper.class})
public interface DriverMapper {


  Driver toEntity(DriverDto driverDto);

  @Mapping(target = "car.driver", ignore = true)
  DriverDto toDto(Driver driver);
}
