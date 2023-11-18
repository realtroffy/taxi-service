package com.modsen.passengerservice.repository;

import com.modsen.passengerservice.model.BankCard;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BankCardRepository extends PagingAndSortingRepository<BankCard, Long> {
}
