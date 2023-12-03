package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.PromoCodeDto;
import com.modsen.rideservice.exception.FinishDateEarlyThanStartDateException;
import com.modsen.rideservice.mapper.PromoCodeMapper;
import com.modsen.rideservice.model.PromoCode;
import com.modsen.rideservice.repository.PromoCodeRepository;
import com.modsen.rideservice.service.PromoCodeService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.String.format;

@Service
@AllArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {

  public static final String DATE_ERROR = "Date from {%s} can't be after the date to {%s}";
  public static final String NO_SUCH_PROMO_CODE_EXCEPTION_MESSAGE =
      "Promo code was not found by id = ";
  private final PromoCodeRepository promoCodeRepository;
  private final PromoCodeMapper promoCodeMapper;

  @Override
  @Transactional(readOnly = true)
  public PromoCodeDto getById(long id) {
    PromoCode promoCode = getPromoCode(id);
    return promoCodeMapper.toDto(promoCode);
  }

  private PromoCode getPromoCode(long id) {
    return promoCodeRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException(NO_SUCH_PROMO_CODE_EXCEPTION_MESSAGE + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PromoCodeDto> getAll(Pageable pageable) {
    List<PromoCodeDto> promoCodeDtoList = new ArrayList<>();
    promoCodeRepository
        .findAll(pageable)
        .forEach(promoCode -> promoCodeDtoList.add(promoCodeMapper.toDto(promoCode)));
    return promoCodeDtoList;
  }

  @Override
  @Transactional
  public PromoCodeDto save(PromoCodeDto promoCodeDto) {
    checkFromIsBeforeTo(promoCodeDto.getStart(), promoCodeDto.getEnd());
    PromoCode promoCode = promoCodeMapper.toEntity(promoCodeDto);
    PromoCode createdPromoCode = promoCodeRepository.save(promoCode);
    return promoCodeMapper.toDto(createdPromoCode);
  }

  @Override
  public void deleteById(long id) {
    promoCodeRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void update(long id, PromoCodeDto promoCodeDto) {
    checkFromIsBeforeTo(promoCodeDto.getStart(), promoCodeDto.getEnd());
    getPromoCode(id);
    promoCodeDto.setId(id);
    PromoCode promoCode = promoCodeMapper.toEntity(promoCodeDto);
    promoCodeRepository.save(promoCode);
  }

  @Override
  public PromoCode getByName(String name) {
    return promoCodeRepository
        .findByName(name)
        .orElseThrow(() -> new NoSuchElementException("Promo code not found by such name " + name));
  }

  private void checkFromIsBeforeTo(LocalDateTime from, LocalDateTime to) {
    if (from.isAfter(to)) {
      throw new FinishDateEarlyThanStartDateException(format(DATE_ERROR, from, to));
    }
  }
}
