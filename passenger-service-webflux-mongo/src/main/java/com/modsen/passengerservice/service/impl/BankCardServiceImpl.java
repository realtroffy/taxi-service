package com.modsen.passengerservice.service.impl;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.mapper.BankCardMapper;
import com.modsen.passengerservice.model.BankCard;
import com.modsen.passengerservice.service.BankCardService;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class BankCardServiceImpl implements BankCardService {

  private static final String NO_SUCH_BANK_CARD_EXCEPTION_MESSAGE = "No element found with id: ";

  private final BankCardMapper bankCardMapper;
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  @Override
  public Mono<BankCardDto> getById(String id) {
    return getEntityById(id).map(bankCardMapper::toDto);
  }

  private Mono<BankCard> getEntityById(String id) {
    return reactiveMongoTemplate
        .findById(id, BankCard.class)
        .switchIfEmpty(
            Mono.error(new NoSuchElementException(NO_SUCH_BANK_CARD_EXCEPTION_MESSAGE + id)));
  }

  @Override
  public Flux<BankCardDto> getAllByPassengerId(ObjectId passengerId) {
    Query query = new Query(Criteria.where("passengerId").is(passengerId));
    return reactiveMongoTemplate.find(query, BankCard.class).map(bankCardMapper::toDto);
  }

  @Override
  public Flux<BankCardDto> getAll(Pageable pageable) {
    Query query = new Query().with(pageable);

    return reactiveMongoTemplate.find(query, BankCard.class)
            .map(bankCardMapper::toDto);
  }

  @Override
  public Mono<BankCardDto> save(BankCardDto bankCardDto) {
    BankCard bankCard = bankCardMapper.toEntity(bankCardDto);
    return reactiveMongoTemplate.save(bankCard).map(bankCardMapper::toDto);
  }

  @Override
  public Mono<BankCardDto> updateById(String id, BankCardDto updatedDto) {
    return getEntityById(id)
        .flatMap(
            existingBankCard -> {
              BankCard updatedBankCard =
                  bankCardMapper.toEntity(updatedDto).setId(existingBankCard.getId());
              return reactiveMongoTemplate.save(updatedBankCard);
            })
        .map(bankCardMapper::toDto);
  }

  @Override
  public Mono<BankCardDto> deleteById(String id) {
    return getEntityById(id)
        .flatMap(
            existingBankCard ->
                reactiveMongoTemplate.remove(existingBankCard).thenReturn(existingBankCard))
        .map(bankCardMapper::toDto);
  }

  @Override
  public Flux<BankCardDto> deleteAllByPassengerId(ObjectId passengerId) {
    Query query = new Query(Criteria.where("passengerId").is(passengerId));
    return reactiveMongoTemplate
        .find(query, BankCard.class)
        .flatMap(
            existingBankCard ->
                reactiveMongoTemplate.remove(existingBankCard).thenReturn(existingBankCard))
        .map(bankCardMapper::toDto);
  }
}
