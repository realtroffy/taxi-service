package com.modsen.driverservice.repository;

import com.modsen.driverservice.model.Car;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CarRepository extends PagingAndSortingRepository<Car, Long> {}
