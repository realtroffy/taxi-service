package com.modsen.driverservice.service;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.dto.DriverRatingDto;
import com.modsen.driverservice.exception.DriverWithoutCarAvailableException;
import com.modsen.driverservice.mapper.DriverMapper;
import com.modsen.driverservice.model.Driver;
import com.modsen.driverservice.repository.DriverRepository;
import com.modsen.driverservice.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

  public static final Long EXIST_ID = 1L;
  public static final Double UPDATED_RATING = 3.0;
  public static final Long NOT_EXIST_ID = 100L;

  private Driver driver;
  private DriverDto driverDto;
  private DriverRatingDto driverRatingDto;

  @Mock private DriverRepository driverRepository;
  @Mock private DriverMapper driverMapper;
  @InjectMocks private DriverServiceImpl driverService;

  @BeforeEach
  void setUp() {
    driver = new Driver();
    driver.setId(EXIST_ID);
    driverDto = new DriverDto();
    driverDto.setId(EXIST_ID);
    driverRatingDto = new DriverRatingDto();
    driverRatingDto.setRating(UPDATED_RATING);
  }

  @Test
  void getDriverById() {
    when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driver));
    when(driverMapper.toDto(driver)).thenReturn(driverDto);

    DriverDto actual = driverService.getById(EXIST_ID);

    assertNotNull(actual);
    assertSame(EXIST_ID, actual.getId());
    verify(driverRepository).findById(anyLong());
  }

  @Test
  void getDtoByIdIfNotExistThanThrowNotSuchElementException() {
    when(driverRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> driverService.getById(NOT_EXIST_ID));
  }

  @Test
  void getAllDrivers() {
    when(driverRepository.findAllIds(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(EXIST_ID)));
    when(driverRepository.findByIdIn(List.of(EXIST_ID), Sort.by("id"))).thenReturn(List.of(driver));
    when(driverMapper.toDto(driver)).thenReturn(driverDto);

    List<DriverDto> actual = driverService.getAll(PageRequest.of(0, 10, Sort.by("id")));

    assertNotNull(actual);
    assertTrue(actual.contains(driverDto));
    assertSame(1, actual.size());
    verify(driverRepository).findAllIds(PageRequest.of(0, 10, Sort.by("id")));
    verify(driverRepository).findByIdIn(List.of(1L), Sort.by("id"));
    verify(driverMapper).toDto(driver);
  }

  @Test
  void saveDriver() {
    when(driverMapper.toEntity(driverDto)).thenReturn(driver);
    when(driverRepository.save(driver)).thenReturn(driver);
    when(driverMapper.toDto(driver)).thenReturn(driverDto);

    DriverDto actual = driverService.save(driverDto);

    assertEquals(driverDto, actual);
    verify(driverMapper).toEntity(driverDto);
    verify(driverRepository).save(driver);
    verify(driverMapper).toDto(driver);
  }

  @Test
  void deleteDriverById() {
    when(driverRepository.findById(EXIST_ID)).thenReturn(Optional.of(driver));

    driverService.deleteById(EXIST_ID);

    verify(driverRepository).deleteById(EXIST_ID);
  }

  @Test
  void deleteByIdIfNotExistThrowNoSuchElementException() {
    when(driverRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> driverService.deleteById(NOT_EXIST_ID));
    verify(driverRepository, never()).deleteById(anyLong());
  }

  @Test
  void updateDriver() {
    when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driver));
    when(driverMapper.toEntity(driverDto)).thenReturn(driver);
    when(driverRepository.save(driver)).thenReturn(driver);

    driverService.update(anyLong(), driverDto);

    verify(driverRepository).save(driver);
    verify(driverMapper).toEntity(driverDto);
  }

  @Test
  void updateIfNotExistThrowNoSuchElementException() {
    driverDto.setId(NOT_EXIST_ID);

    assertThrows(NoSuchElementException.class, () -> driverService.update(NOT_EXIST_ID, driverDto));
    verify(driverRepository, never()).save(driver);
  }

  @Test
  void updateIfCarIsNullAndAvailableTrueThanThrowDriverWithoutCarAvailableException() {
    driverDto.setIsAvailable(true);

    assertThrows(
        DriverWithoutCarAvailableException.class,
        () -> driverService.update(NOT_EXIST_ID, driverDto));
    verify(driverRepository, never()).save(driver);
  }

  @Test
  void updateDriverRating() {
    when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driver));
    when(driverRepository.save(driver)).thenReturn(driver);
    driverDto.setRating(UPDATED_RATING);
    when(driverMapper.toDto(driver)).thenReturn(driverDto);

    DriverDto actual = driverService.updateRating(EXIST_ID, driverRatingDto);

    assertNotNull(actual);
    assertEquals(UPDATED_RATING, driverDto.getRating());
    verify(driverRepository).findById(anyLong());
    verify(driverRepository).save(driver);
    verify(driverMapper).toDto(driver);
  }

  @Test
  void updateRatingIfNotExistThanThrowNoSuchElementException() {
    when(driverRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class,
        () -> driverService.updateRating(NOT_EXIST_ID, driverRatingDto));
    verify(driverRepository, never()).save(driver);
  }

  @Test
  void updateAvailabilityToTrueAfterFinishedRide() {
    when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driver));
    when(driverRepository.save(any(Driver.class))).thenReturn(driver);
    when(driverMapper.toDto(any(Driver.class))).thenReturn(driverDto);

    driverService.updateAvailabilityToTrueAfterFinishedRide(EXIST_ID);

    verify(driverRepository).findById(anyLong());
    verify(driverRepository).save(any(Driver.class));
    verify(driverMapper).toDto(any(Driver.class));
  }

  @Test
  void updateAvailabilityToTrueAfterFinishedRideIfNotExistThrowNoSuchElementException() {
    when(driverRepository.findById(NOT_EXIST_ID)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class,
        () -> driverService.updateAvailabilityToTrueAfterFinishedRide(NOT_EXIST_ID));
    verify(driverRepository, never()).save(driver);
  }

  @Test
  void getDriversByIds() {
    when(driverRepository.findByIdIn(List.of(EXIST_ID), null)).thenReturn(List.of(driver));
    when(driverMapper.toDto(driver)).thenReturn(driverDto);

    driverService.getDriversByIds(List.of(EXIST_ID));

    verify(driverRepository).findByIdIn(List.of(EXIST_ID), null);
    verify(driverMapper).toDto(driver);
  }
}
