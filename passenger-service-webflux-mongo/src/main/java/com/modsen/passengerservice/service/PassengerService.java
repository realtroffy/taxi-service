package com.modsen.passengerservice.service;

import com.modsen.passengerservice.dto.PassengerDto;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PassengerService {

    Mono<PassengerDto> getById(String id);

    Flux<PassengerDto> getAll(Pageable pageable);

    Mono<PassengerDto> save(PassengerDto passengerDto);

    Mono<PassengerDto> updateById(String id, PassengerDto updatedDto);

    Mono<PassengerDto> deleteByIdWithBankCards(String id);
}
