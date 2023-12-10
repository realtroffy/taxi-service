package com.modsen.passengerservice.integration.repository;

import com.modsen.passengerservice.integration.testbase.IntegrationTestBase;
import com.modsen.passengerservice.model.Passenger;
import com.modsen.passengerservice.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
class PassengerRepositoryIT extends IntegrationTestBase {

  public static final Long EXISTED_FIRST_PASSENGER_ID = 22L;
  public static final Long EXISTED_SECOND_PASSENGER_ID = 66L;
  public static final Long EXISTED_THIRD_PASSENGER_ID = 100L;
  public static final int COUNT_EXISTED_ENTITY_IN_DB = 3;
  public static final int INDEX_FIRST_ELEMENT = 0;
  public static final int INDEX_THIRD_ELEMENT = 2;

  private Pageable pageableFirstEntity;
  private Pageable pageableSecondEntity;
  private Pageable pageableAllEntity;

  private final PassengerRepository passengerRepository;

  @BeforeEach
  void setUp() {
    pageableFirstEntity = PageRequest.of(0, 1);
    pageableSecondEntity = PageRequest.of(1, 1);
    pageableAllEntity = PageRequest.of(0, 20);
  }

  @Test
  void getFirstExistedEntityIdByPageable() {
    Page<BigInteger> allIds = passengerRepository.findAllIds(pageableFirstEntity);
    BigInteger ids = allIds.get().collect(Collectors.toList()).get(INDEX_FIRST_ELEMENT);
    Optional<Passenger> actualResult = getPassenger(ids);

    actualResult.ifPresent(actual -> assertEquals(EXISTED_FIRST_PASSENGER_ID, actual.getId()));
  }

  @Test
  void getSecondExistedEntityIdByPageable() {
    Page<BigInteger> allIds = passengerRepository.findAllIds(pageableSecondEntity);
    BigInteger ids = allIds.get().collect(Collectors.toList()).get(INDEX_FIRST_ELEMENT);
    Optional<Passenger> actualResult = getPassenger(ids);

    actualResult.ifPresent(actual -> assertEquals(EXISTED_SECOND_PASSENGER_ID, actual.getId()));
  }

  @Test
  void getAllExistEntityIdsByPageable() {
    Page<BigInteger> allIds = passengerRepository.findAllIds(pageableAllEntity);
    List<BigInteger> listIds = allIds.get().collect(Collectors.toList());
    Optional<Passenger> actualResult =
        passengerRepository.findById(listIds.get(INDEX_THIRD_ELEMENT).longValue());

    actualResult.ifPresent(
        actual -> assertEquals(EXISTED_THIRD_PASSENGER_ID, actualResult.get().getId()));
    assertEquals(COUNT_EXISTED_ENTITY_IN_DB, listIds.size());
  }

  private Optional<Passenger> getPassenger(BigInteger ids) {
    return passengerRepository.findById(ids.longValue());
  }
}
