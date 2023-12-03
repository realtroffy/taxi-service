package com.modsen.driverservice.repository;

import com.modsen.driverservice.model.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DriverRepository extends PagingAndSortingRepository<Driver, Long> {

    @EntityGraph(attributePaths = {"bankCards", "car"})
    Optional<Driver> findById(Long id);

    @EntityGraph(attributePaths = {"bankCards", "car"})
    List<Driver> findByIdIn(Collection<Long> id, Sort sort);

    @Query("SELECT d.id FROM Driver d")
    Page<Long> findAllIds(Pageable pageable);

    @Query("SELECT d.id FROM Driver d WHERE d.isAvailable=:isAvailable")
    Page<Long> findAllIdsByAvailable(@Param("isAvailable") boolean isAvailable, Pageable pageable);

    @Query(value = "SELECT * FROM drivers WHERE is_available = true ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Driver> findRandomAvailable();
}
