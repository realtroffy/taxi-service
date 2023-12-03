package com.modsen.rideservice.mapper;

import com.modsen.rideservice.dto.RideDto;
import com.modsen.rideservice.model.Ride;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RideMapper {

  @Mapping(source = "promoCodeName", target = "promoCode.name")
  Ride toEntity(RideDto rideDto);

  @Mapping(source = "promoCode.name", target = "promoCodeName")
  RideDto toDto(Ride ride);
}
