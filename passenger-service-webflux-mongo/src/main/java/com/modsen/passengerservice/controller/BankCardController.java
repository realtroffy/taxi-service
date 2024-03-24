package com.modsen.passengerservice.controller;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.service.BankCardService;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/bankcards")
public class BankCardController {

  private final BankCardService bankCardService;

  @GetMapping("/{id}")
  public Mono<BankCardDto> getById(@PathVariable("id") String id) {
    return bankCardService.getById(id);
  }

  @GetMapping
  public Flux<BankCardDto> getAll(Pageable pageable) {
    return bankCardService.getAll(pageable);
  }

  @PostMapping
  @ResponseStatus(value = HttpStatus.CREATED)
  public Mono<BankCardDto> save(@Valid @RequestBody BankCardDto bankCardDto) {
    return bankCardService.save(bankCardDto);
  }

  @GetMapping("/all/{passengerId}")
  public Flux<BankCardDto> getAllBankCardsByPassengerId(
      @PathVariable("passengerId") ObjectId passengerId) {
    return bankCardService.getAllByPassengerId(passengerId);
  }

  @PutMapping("/{id}")
  public Mono<BankCardDto> updateById(
      @PathVariable("id") String id, @Valid @RequestBody BankCardDto bankCardDto) {
    return bankCardService.updateById(id, bankCardDto);
  }

  @DeleteMapping("/{id}")
  public Mono<BankCardDto> deleteById(@PathVariable("id") String id) {
    return bankCardService.deleteById(id);
  }

  @DeleteMapping("/all/{passengerId}")
  public Flux<BankCardDto> deleteAllByPassengerId(@PathVariable("passengerId") ObjectId passengerId) {
    return bankCardService.deleteAllByPassengerId(passengerId);
  }
}
