package com.modsen.rideservice.unit.service;

import com.modsen.rideservice.dto.PromoCodeDto;
import com.modsen.rideservice.exception.FinishDateEarlyThanStartDateException;
import com.modsen.rideservice.mapper.PromoCodeMapper;
import com.modsen.rideservice.model.PromoCode;
import com.modsen.rideservice.repository.PromoCodeRepository;
import com.modsen.rideservice.service.impl.PromoCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceImplTest {

  public static final Long EXIST_ID = 1L;
  public static final Long NOT_EXIST_ID = 100L;

  private PromoCodeDto promoCodeDto;
  private PromoCode promoCode;
  @Mock private PromoCodeRepository promoCodeRepository;
  @Mock private PromoCodeMapper promoCodeMapper;

  @InjectMocks private PromoCodeServiceImpl promoCodeService;

  @BeforeEach
  void setUp() {
    promoCodeDto =
        PromoCodeDto.builder()
            .id(1L)
            .name("SUPER20")
            .discount(BigDecimal.valueOf(0.2))
            .start(LocalDateTime.of(2022, 10, 10, 10, 10))
            .end(LocalDateTime.of(2023, 10, 10, 10, 10))
            .build();

    promoCode = new PromoCode();
    promoCode.setId(1L);
    promoCode.setName("SUPER20");
    promoCode.setDiscount(BigDecimal.valueOf(0.2));
    promoCode.setStart(LocalDateTime.of(2022, 10, 10, 10, 10));
    promoCode.setEnd(LocalDateTime.of(2023, 10, 10, 10, 10));
  }

  @Test
  void getDtoById() {
    when(promoCodeRepository.findById(anyLong())).thenReturn(Optional.of(promoCode));
    when(promoCodeMapper.toDto(promoCode)).thenReturn(promoCodeDto);

    PromoCodeDto actual = promoCodeService.getById(EXIST_ID);

    assertNotNull(actual);
    verify(promoCodeRepository).findById(anyLong());
  }

  @Test
  void getDtoByIdIfNotExistThanThrowNotSuchElementException() {
    when(promoCodeRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> promoCodeService.getById(NOT_EXIST_ID));
  }

  @Test
  void getAll() {
    when(promoCodeRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(promoCode)));
    when(promoCodeMapper.toDto(promoCode)).thenReturn(promoCodeDto);

    List<PromoCodeDto> actual = promoCodeService.getAll(PageRequest.of(0, 10));

    assertNotNull(actual);
    assertSame(1, actual.size());
    verify(promoCodeRepository).findAll(any(Pageable.class));
    verify(promoCodeMapper).toDto(promoCode);
  }

  @Test
  void save() {
    when(promoCodeMapper.toEntity(promoCodeDto)).thenReturn(promoCode);
    when(promoCodeRepository.save(promoCode)).thenReturn(promoCode);
    when(promoCodeMapper.toDto(promoCode)).thenReturn(promoCodeDto);

    PromoCodeDto actual = promoCodeService.save(promoCodeDto);

    assertEquals(promoCodeDto, actual);
    verify(promoCodeMapper).toEntity(promoCodeDto);
    verify(promoCodeRepository).save(promoCode);
    verify(promoCodeMapper).toDto(promoCode);
  }

  @Test
  void saveWhenFinishDateEarlyStartDateThanThrowFinishDateEarlyThanStartDateException() {
    promoCodeDto.setStart(LocalDateTime.now());
    assertThrows(
        FinishDateEarlyThanStartDateException.class, () -> promoCodeService.save(promoCodeDto));

    verify(promoCodeRepository, never()).save(promoCode);
  }

  @Test
  void deleteById() {
    when(promoCodeRepository.findById(EXIST_ID)).thenReturn(Optional.of(promoCode));

    promoCodeService.deleteById(EXIST_ID);

    verify(promoCodeRepository).deleteById(EXIST_ID);
  }

  @Test
  void deleteByIdIfNotExistThrowNoSuchElementException() {
    when(promoCodeRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> promoCodeService.deleteById(NOT_EXIST_ID));
    verify(promoCodeRepository, never()).deleteById(anyLong());
  }

  @Test
  void update() {
    when(promoCodeRepository.findById(anyLong())).thenReturn(Optional.of(promoCode));
    when(promoCodeMapper.toEntity(promoCodeDto)).thenReturn(promoCode);
    when(promoCodeRepository.save(promoCode)).thenReturn(promoCode);

    promoCodeService.update(EXIST_ID, promoCodeDto);

    verify(promoCodeRepository).findById(anyLong());
    verify(promoCodeRepository).save(promoCode);
    verify(promoCodeMapper).toEntity(promoCodeDto);
  }

  @Test
  void updateIfNotExistThrowNoSuchElementException() {
    when(promoCodeRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class, () -> promoCodeService.update(NOT_EXIST_ID, promoCodeDto));
    verify(promoCodeRepository, never()).save(promoCode);
  }

  @Test
  void getByNameIfExist() {
    when(promoCodeRepository.findByName("SUPER20")).thenReturn(Optional.of(promoCode));

    PromoCode actual = promoCodeService.getByName("SUPER20");

    assertEquals(promoCode, actual);
    verify(promoCodeRepository).findByName("SUPER20");
  }

  @Test
  void getByNameIfNotExist() {
    when(promoCodeRepository.findByName("SUPER30")).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> promoCodeService.getByName("SUPER30"));
  }
}
