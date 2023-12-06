package com.modsen.rideservice.service;

import com.modsen.rideservice.dto.PromoCodeDto;
import com.modsen.rideservice.model.PromoCode;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromoCodeService {

  PromoCodeDto getById(long id);

  List<PromoCodeDto> getAll(Pageable pageable);

  PromoCodeDto save(PromoCodeDto promoCodeDto);

  void deleteById(long id);

  void update(long id, PromoCodeDto promoCodeDto);

  PromoCode getByName(String name);
}
