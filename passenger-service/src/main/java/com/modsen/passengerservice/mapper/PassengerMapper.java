package com.modsen.passengerservice.mapper;

import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.model.Passenger;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BankCardMapper.class)
public interface PassengerMapper {

  Passenger toEntity(PassengerDto passengerDto);

  PassengerDto toDto(Passenger passenger);
}
