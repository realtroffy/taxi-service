package com.modsen.passengerservice.mapper;

import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.model.Passenger;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

  PassengerDto toDto(Passenger passenger);

  Passenger toEntity(PassengerDto passengerDto);
}
