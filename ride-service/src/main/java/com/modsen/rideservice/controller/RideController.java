package com.modsen.rideservice.controller;

import com.modsen.rideservice.dto.PassengerRatingFinishDto;
import com.modsen.rideservice.dto.RideDto;
import com.modsen.rideservice.dto.RidePageDto;
import com.modsen.rideservice.service.RideService;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/rides")
public class RideController {

  private final RideService rideService;

  @PostMapping
  public ResponseEntity<RideDto> order(@RequestBody @Valid RideDto rideDto) {
    return ResponseEntity.status(CREATED).body(rideService.order(rideDto));
  }

  @PutMapping("{rideId}/finish")
  public ResponseEntity<Void> finishByDriver(
      @PathVariable(name = "rideId") Long rideId,
      @RequestBody @Valid PassengerRatingFinishDto passengerRatingFinishDto) {
    rideService.finishByDriver(rideId, passengerRatingFinishDto);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("{rideId}/cancel")
  public ResponseEntity<RideDto> cancelByPassenger(@PathVariable(name = "rideId") Long rideId) {
    RideDto rideDto = rideService.cancelByPassenger(rideId);
    return ResponseEntity.status(CREATED).body(rideDto);
  }

  @PutMapping("{rideId}/{driverRating}")
  public ResponseEntity<Void> updateDriverRatingAfterFinishRide(
      @PathVariable(name = "rideId") Long rideId,
      @PathVariable(name = "driverRating") @Min(value = 0) @Max(value = 5) Integer driverRating) {
    rideService.updateDriverRatingAfterRide(rideId, driverRating);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<RideDto> getById(@PathVariable("id") long id) {
    return ResponseEntity.ok(rideService.getById(id));
  }

  @GetMapping
  public ResponseEntity<RidePageDto> getAll(Pageable pageable) {
    List<RideDto> rideDtoList = rideService.getAll(pageable);
    RidePageDto ridePageDto = RidePageDto.builder().rideDtoList(rideDtoList).build();
    return ResponseEntity.ok(ridePageDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    rideService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable("id") long id, @Valid @RequestBody RideDto rideDto) {
    rideService.update(id, rideDto);
    return ResponseEntity.noContent().build();
  }
}
