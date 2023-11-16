package com.modsen.passengerservice.repository;

import com.modsen.passengerservice.model.Passenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PassengerRepository extends PagingAndSortingRepository<Passenger, Long> {

  @EntityGraph(attributePaths = "bankCards")
  Optional<Passenger> findById(Long id);

  @EntityGraph(attributePaths = "bankCards")
  List<Passenger> findByIdIn(Collection<Long> id);

  @Query(value = "SELECT id FROM passengers", nativeQuery = true)
  Page<BigInteger> findAllIds(Pageable pageable);
}
