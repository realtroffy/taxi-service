package com.modsen.passengerservice.controller;

import com.modsen.passengerservice.dto.PassengerAfterRideDto;
import com.modsen.passengerservice.dto.PassengerDto;
import com.modsen.passengerservice.service.PassengerService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/passengers")
public class PassengerController {

  private final PassengerService passengerService;

  @GetMapping("/{id}")
  public ResponseEntity<PassengerDto> getById(@PathVariable("id") long id) {
    return ResponseEntity.ok(passengerService.getById(id));
  }

  @GetMapping()
  public ResponseEntity<List<PassengerDto>> getAll(Pageable pageable) {
    List<PassengerDto> passengers = passengerService.getAll(pageable);
    return ResponseEntity.ok(passengers);
  }

  @PostMapping
  public ResponseEntity<PassengerDto> save(@RequestBody @Valid PassengerDto passengerDto) {
    return ResponseEntity.status(CREATED).body(passengerService.save(passengerDto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable("id") long id, @Valid @RequestBody PassengerDto passengerDto) {
    passengerService.update(id, passengerDto);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/{rating}")
  public ResponseEntity<PassengerDto> updateRating(
      @PathVariable("id") long id,
      @PathVariable("rating") @DecimalMin(value = "0") @DecimalMax(value = "5.0") double rating) {
    return ResponseEntity.status(OK).body(passengerService.updateRating(id, rating));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    passengerService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{passengerId}/bankcards/{bankCardId}")
  public ResponseEntity<Void> addBankCardToPassenger(
      @PathVariable("passengerId") long passengerId, @PathVariable("bankCardId") long bankCardId) {
    passengerService.addBankCardToPassenger(passengerId, bankCardId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{passengerId}/bankcards/{bankCardId}")
  public ResponseEntity<Void> deleteBankCardFromPassenger(
      @PathVariable("passengerId") long passengerId, @PathVariable("bankCardId") long bankCardId) {
    passengerService.removeBankCardToPassenger(passengerId, bankCardId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/after-ride/{id}")
  public ResponseEntity<Void> updateAfterRide(
      @PathVariable("id") long id,
      @Valid @RequestBody PassengerAfterRideDto passengerAfterRideDto) {
    passengerService.updateAfterRide(id, passengerAfterRideDto);
    return ResponseEntity.noContent().build();
  }
}
