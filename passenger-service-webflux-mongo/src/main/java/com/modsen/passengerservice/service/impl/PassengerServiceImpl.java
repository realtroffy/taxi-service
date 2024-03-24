package com.modsen.passengerservice.service.impl;

import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.mapper.PassengerMapper;
import com.modsen.passengerservice.model.Passenger;
import com.modsen.passengerservice.service.BankCardService;
import com.modsen.passengerservice.service.PassengerService;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class PassengerServiceImpl implements PassengerService {

  private static final String NO_SUCH_PASSENGER_EXCEPTION_MESSAGE = "No element found with id: ";
  private static final Double DEFAULT_RATING_NEW_PASSENGER = 5.0;

  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final PassengerMapper passengerMapper;
  private final BankCardService bankCardService;

  @Override
  public Mono<PassengerDto> getById(String id) {
    return getEntityById(id).map(passengerMapper::toDto);
  }

  private Mono<Passenger> getEntityById(String id) {
    return reactiveMongoTemplate
        .findById(id, Passenger.class)
        .switchIfEmpty(
            Mono.error(new NoSuchElementException(NO_SUCH_PASSENGER_EXCEPTION_MESSAGE + id)));
  }

  @Override
  public Flux<PassengerDto> getAll(Pageable pageable) {
    Query query = new Query().with(pageable);

    return reactiveMongoTemplate.find(query, Passenger.class)
            .map(passengerMapper::toDto);
  }

  @Override
  public Mono<PassengerDto> save(PassengerDto passengerDto) {
    Passenger passenger =
        passengerMapper.toEntity(passengerDto).setRating(DEFAULT_RATING_NEW_PASSENGER);
    return reactiveMongoTemplate.save(passenger).map(passengerMapper::toDto);
  }

  @Override
  public Mono<PassengerDto> updateById(String id, PassengerDto updatedDto) {
    return getEntityById(id)
        .flatMap(
            existingPassenger -> {
              Passenger updatedPassenger =
                  passengerMapper.toEntity(updatedDto).setId(existingPassenger.getId());
              return reactiveMongoTemplate.save(updatedPassenger);
            })
        .map(passengerMapper::toDto);
  }

  @Override
  @Transactional
  public Mono<PassengerDto> deleteByIdWithBankCards(String id) {
    return getEntityById(id)
            .flatMap(existingPassenger -> bankCardService.deleteAllByPassengerId(new ObjectId(existingPassenger.getId()))
                    .then(reactiveMongoTemplate.remove(existingPassenger).thenReturn(existingPassenger))
                    .map(passengerMapper::toDto));
  }
}
