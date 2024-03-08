package com.modsen.passengerservice.service;

import com.modsen.passengerservice.dto.BankCardDto;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankCardService {

  Mono<BankCardDto> getById(String id);

  Flux<BankCardDto> getAllByPassengerId(ObjectId passengerId);

  Flux<BankCardDto> getAll(Pageable pageable);

  Mono<BankCardDto> save(BankCardDto bankCardDto);

  Mono<BankCardDto> deleteById(String id);

  Flux<BankCardDto> deleteAllByPassengerId(ObjectId passengerId);

  Mono<BankCardDto> updateById(String id, BankCardDto updatedDto);
}
