package com.modsen.passengerservice.mapper;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.model.BankCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankCardMapper {


    @Mapping(source = "passenger.id", target = "passengerId")
    BankCardDto toDto(BankCard bankCard);


    @Mapping(source = "passengerId", target = "passenger.id")
    BankCard toEntity(BankCardDto bankCardDto);
}
