package com.modsen.driverservice.mapper;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.dto.DriverRideDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverDtoToDriverRideDtoMapper {

  @Mapping(target = "rideId", ignore = true)
  DriverRideDto toDriverRideDto(DriverDto driverDto);
}
