package com.modsen.passengerservice.mapper;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.model.BankCard;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BankCardMapper {

  @Mapping(target = "passengerId", source = "passengerId", qualifiedByName = "objectIdToString")
  BankCardDto toDto(BankCard bankCard);

  @Mapping(target = "passengerId", source = "passengerId", qualifiedByName = "stringToObjectId")
  BankCard toEntity(BankCardDto bankCardDto);

  @Named("stringToObjectId")
  default ObjectId map(String value) {
    return value != null ? new ObjectId(value) : null;
  }

  @Named("objectIdToString")
  default String map(ObjectId value) {
    return value != null ? value.toHexString() : null;
  }
}
