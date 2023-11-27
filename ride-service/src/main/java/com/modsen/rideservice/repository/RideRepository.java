package com.modsen.rideservice.repository;

import com.modsen.rideservice.model.Ride;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RideRepository extends PagingAndSortingRepository<Ride, Long> {

  @Query("FROM Ride WHERE passengerId=:passengerId AND finishTime IS NULL")
  List<Ride> findByPassengerIdAndFinishTimeNotNull(@Param("passengerId") Long passengerId);

  @Query("SELECT AVG(passengerRating) FROM Ride WHERE passengerId=:passengerId")
  Double findAveragePassengerRatingByPassengerId(@Param("passengerId") Long passengerId);

  @Query("SELECT AVG(driverRating) FROM Ride WHERE driverId=:driverId")
  Double findAverageDriverRatingByDriverId(@Param("driverId") Long driverId);
}
