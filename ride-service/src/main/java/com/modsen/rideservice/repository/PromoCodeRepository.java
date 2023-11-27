package com.modsen.rideservice.repository;

import com.modsen.rideservice.model.PromoCode;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface PromoCodeRepository extends PagingAndSortingRepository<PromoCode, Long> {

    Optional<PromoCode> findByName(String name);
}
