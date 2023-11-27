package com.modsen.rideservice.mapper;

import com.modsen.rideservice.dto.PromoCodeDto;
import com.modsen.rideservice.model.PromoCode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromoCodeMapper {

  PromoCode toEntity(PromoCodeDto promoCodeDto);

  PromoCodeDto toDto(PromoCode promoCode);
}
