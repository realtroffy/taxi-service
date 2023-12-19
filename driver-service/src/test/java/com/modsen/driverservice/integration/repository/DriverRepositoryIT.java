package com.modsen.driverservice.integration.repository;

import com.modsen.driverservice.integration.testbase.IntegrationTestBase;
import com.modsen.driverservice.model.Driver;
import com.modsen.driverservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
class DriverRepositoryIT extends IntegrationTestBase {

  public static final Long EXISTED_DRIVER_ID_WITH_AVAILABILITY_TRUE = 22L;

  private final DriverRepository driverRepository;

  @Test
  void findRandomAvailableIfExist() {
    Optional<Driver> actualRandomAvailableDriver = driverRepository.findRandomAvailable();

    actualRandomAvailableDriver.ifPresent(
        driver -> assertEquals(EXISTED_DRIVER_ID_WITH_AVAILABILITY_TRUE, driver.getId()));
  }
}
