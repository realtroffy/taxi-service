package com.modsen.driverservice.mapper;

import com.modsen.driverservice.dto.BankCardDto;
import com.modsen.driverservice.model.BankCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankCardMapper {

  @Mapping(source = "driver.id", target = "driverId")
  BankCardDto toDto(BankCard bankCard);

  @Mapping(source = "driverId", target = "driver.id")
  BankCard toEntity(BankCardDto bankCardDto);
}
