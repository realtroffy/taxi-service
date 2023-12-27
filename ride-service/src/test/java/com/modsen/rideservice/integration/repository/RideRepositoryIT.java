package com.modsen.rideservice.integration.repository;

import com.modsen.rideservice.integration.testenvironment.IntegrationTestEnvironment;
import com.modsen.rideservice.model.Ride;
import com.modsen.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
class RideRepositoryIT extends IntegrationTestEnvironment {

  public static final Long EXIST_DRIVER_ID = 3L;
  public static final Long EXIST_PASSENGER_ID = 3L;
  public static final Long EXIST_RIDE_ID_WITH_NULL_FINISH_TIME = 2L;
  public static final Long EXIST_PASSENGER_ID_WITH_FINISH_TIME_NULL = 2L;
  public static final Double EXPECTED_DRIVER_RATING = 2.0;
  public static final Double EXPECTED_PASSENGER_RATING = 4.0;
  public static final int RIDE_INDEX_WITH_NULL_FINISH_TIME = 0;

  private final RideRepository rideRepository;

  @Test
  void findAverageDriverRatingByDriverId() {
    Double actualRating = rideRepository.findAverageDriverRatingByDriverId(EXIST_DRIVER_ID);

    assertEquals(EXPECTED_DRIVER_RATING, actualRating);
  }

  @Test
  void findAveragePassengerRatingByPassengerId() {
    Double actualRating =
        rideRepository.findAveragePassengerRatingByPassengerId(EXIST_PASSENGER_ID);

    assertEquals(EXPECTED_PASSENGER_RATING, actualRating);
  }

  @Test
  void findByPassengerIdAndFinishTimeNotNull() {
    List<Ride> byPassengerIdAndFinishTimeNotNull =
        rideRepository.findByPassengerIdAndFinishTimeNotNull(
            EXIST_PASSENGER_ID_WITH_FINISH_TIME_NULL);
    Ride actual = byPassengerIdAndFinishTimeNotNull.get(RIDE_INDEX_WITH_NULL_FINISH_TIME);
    Ride expected = rideRepository.findById(EXIST_RIDE_ID_WITH_NULL_FINISH_TIME).get();

    assertEquals(expected, actual);
  }
}
