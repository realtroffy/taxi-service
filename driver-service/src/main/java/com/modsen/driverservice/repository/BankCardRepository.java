package com.modsen.driverservice.repository;

import com.modsen.driverservice.model.BankCard;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BankCardRepository extends PagingAndSortingRepository<BankCard, Long> {
}
