package com.modsen.driverservice.controller;

import com.modsen.driverservice.dto.DriverDto;
import com.modsen.driverservice.dto.DriverPageDto;
import com.modsen.driverservice.dto.IdPageDto;
import com.modsen.driverservice.service.DriverService;
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
@RequestMapping("/api/v1/drivers")
public class DriverController {

  private final DriverService driverService;

  @GetMapping("/{id}")
  public ResponseEntity<DriverDto> getById(@PathVariable("id") long id) {
    return ResponseEntity.ok(driverService.getById(id));
  }

  @GetMapping()
  public ResponseEntity<DriverPageDto> getAll(Pageable pageable) {
    List<DriverDto> driverDtoList = driverService.getAll(pageable);
    DriverPageDto driverPageDto = DriverPageDto.builder().driverDtoList(driverDtoList).build();
    return ResponseEntity.ok(driverPageDto);
  }

  @PostMapping
  public ResponseEntity<DriverDto> save(@RequestBody @Valid DriverDto driverDto) {
    return ResponseEntity.status(CREATED).body(driverService.save(driverDto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable("id") long id, @Valid @RequestBody DriverDto driverDto) {
    driverService.update(id, driverDto);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/{rating}")
  public ResponseEntity<DriverDto> updateRating(
      @PathVariable("id") long id,
      @PathVariable("rating") @DecimalMin(value = "0") @DecimalMax(value = "5.0") Double rating) {
    return ResponseEntity.status(OK).body(driverService.updateRating(id, rating));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    driverService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{driverId}/bankcards/{bankCardId}")
  public ResponseEntity<Void> addBankCardToDriver(
      @PathVariable("driverId") long driverId, @PathVariable("bankCardId") long bankCardId) {
    driverService.addBankCardToDriver(driverId, bankCardId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{driverId}/bankcards/{bankCardId}")
  public ResponseEntity<Void> deleteBankCardFromDriver(
      @PathVariable("driverId") long driverId, @PathVariable("bankCardId") long bankCardId) {
    driverService.removeBankCardToDriver(driverId, bankCardId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{driverId}/available-true")
  public ResponseEntity<Void> updateAvailabilityToTrueAfterFinishedRide(
      @PathVariable("driverId") long driverId) {
    driverService.updateAvailabilityToTrueAfterFinishedRide(driverId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/list-id")
  public ResponseEntity<DriverPageDto> getDriversByIds(@RequestBody IdPageDto idPageDto) {
    List<DriverDto> driverDtoList = driverService.getDriversByIds(idPageDto.getListId());
    DriverPageDto driverPageDto = DriverPageDto.builder().driverDtoList(driverDtoList).build();
    return ResponseEntity.ok(driverPageDto);
  }
}
