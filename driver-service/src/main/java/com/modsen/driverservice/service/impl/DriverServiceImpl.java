package com.modsen.driverservice.service.impl;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.mapper.BankCardMapper;
import com.modsen.driverservice.mapper.DriverMapper;
import com.modsen.driverservice.model.BankCard;
import com.modsen.driverservice.model.Driver;
import com.modsen.driverservice.repository.DriverRepository;
import com.modsen.driverservice.service.BankCardService;
import com.modsen.driverservice.service.DriverService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DriverServiceImpl implements DriverService {

  public static final String NO_SUCH_DRIVER_EXCEPTION_MESSAGE = "Driver was not found by id = ";
  public static final Double DEFAULT_RATING_NEW_DRIVER = 5.0;
  public static final boolean DEFAULT_AVAILABILITY_NEW_DRIVER = false;
  private final DriverRepository driverRepository;
  private final BankCardService bankCardService;
  private final BankCardMapper bankCardMapper;
  private final DriverMapper driverMapper;

  @Override
  @Transactional(readOnly = true)
  public DriverDto getById(long id) {
    Driver driver = getDriver(id);
    DriverDto driverDto = driverMapper.toDto(driver);
    driverDto.setRating(driver.getRating());
    return driverDto;
  }

  private Driver getDriver(long id) {
    return driverRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException(NO_SUCH_DRIVER_EXCEPTION_MESSAGE + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<DriverDto> getAll(Pageable pageable) {
    Page<Long> ids =
        driverRepository.findAllIds(pageable);
    List<DriverDto> drivers = new ArrayList<>();
    List<Driver> byIdIn = driverRepository.findByIdIn(ids.toList(), pageable.getSort());
    byIdIn.forEach(
        driver -> {
          DriverDto driverDto = driverMapper.toDto(driver);
          driverDto.setRating(driver.getRating());
          drivers.add(driverDto);
        });

    return drivers;
  }

  @Override
  @Transactional
  public DriverDto save(DriverDto driverDto) {
    Driver driver = driverMapper.toEntity(driverDto);
    driver.setAvailable(DEFAULT_AVAILABILITY_NEW_DRIVER);
    driver.setRating(DEFAULT_RATING_NEW_DRIVER);
    Driver createdDriver = driverRepository.save(driver);
    DriverDto dto = driverMapper.toDto(createdDriver);
    dto.setRating(DEFAULT_RATING_NEW_DRIVER);
    return dto;
  }

  @Override
  @Transactional
  public void deleteById(long id) {
    driverRepository.deleteById(id);
  }

  @Override
  @Transactional
  public void update(long id, DriverDto driverDto) {
    Driver driverFromDB = getDriver(id);
    driverDto.setId(id);
    Driver driver = driverMapper.toEntity(driverDto);
    driver.setRating(driverFromDB.getRating());
    driverRepository.save(driver);
  }

  @Override
  @Transactional
  public DriverDto updateRating(long id, double rating) {
    Driver driver = getDriver(id);
    driver.setRating(rating);
    DriverDto driverDto = driverMapper.toDto(driverRepository.save(driver));
    driverDto.setRating(rating);
    return driverDto;
  }

  @Override
  @Transactional
  public void addBankCardToDriver(long driverId, long bankCardId) {
    Driver driver = getDriver(driverId);
    BankCard bankCard = bankCardMapper.toEntity(bankCardService.getById(bankCardId));
    driver.addCard(bankCard);
  }

  @Override
  @Transactional
  public void removeBankCardToDriver(long driverId, long bankCardId) {
    Driver driver = getDriver(driverId);
    BankCard bankCard = bankCardMapper.toEntity(bankCardService.getById(bankCardId));
    driver.removeBankCard(bankCard);
  }
}
