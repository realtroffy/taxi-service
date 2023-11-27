package com.modsen.rideservice.mapper;

import com.modsen.rideservice.dto.RideDto;
import com.modsen.rideservice.model.Ride;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RideMapper {

  Ride toEntity(RideDto rideDto);

  RideDto toDto(Ride ride);
}
