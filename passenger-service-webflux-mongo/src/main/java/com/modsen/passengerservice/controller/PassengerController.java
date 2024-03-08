package com.modsen.passengerservice.controller;

import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.service.PassengerService;
import lombok.AllArgsConstructor;
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
@RequestMapping("/api/v1/passengers")
public class PassengerController {

  private final PassengerService passengerService;

  @GetMapping("/{id}")
  public Mono<PassengerDto> getById(@PathVariable("id") String id) {
    return passengerService.getById(id);
  }

  @GetMapping
  public Flux<PassengerDto> getAll(Pageable pageable) {
    return passengerService.getAll(pageable);
  }

  @PostMapping
  @ResponseStatus(value = HttpStatus.CREATED)
  public Mono<PassengerDto> save(@Valid @RequestBody PassengerDto passengerDto) {
    return passengerService.save(passengerDto);
  }

  @DeleteMapping("/{id}")
  public Mono<PassengerDto> deleteByIdWithBankCards(@PathVariable("id") String id) {
    return passengerService.deleteByIdWithBankCards(id);
  }

  @PutMapping("/{id}")
  public Mono<PassengerDto> updateById(
          @PathVariable("id") String id, @Valid @RequestBody PassengerDto passengerDto) {
    return passengerService.updateById(id, passengerDto);
  }
}
