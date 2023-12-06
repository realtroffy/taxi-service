package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.PassengerAfterRideDto;
import com.modsen.rideservice.dto.PassengerDto;
import com.modsen.rideservice.exception.ServerUnavailableException;
import com.modsen.rideservice.model.Ride;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.NoSuchElementException;

import static reactor.core.publisher.Mono.error;

@Component
public class PassengerServiceWebClient {

  private final WebClient webClient;

  public PassengerServiceWebClient(@Qualifier("passengerWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  public ResponseEntity<PassengerDto> getPassengerDtoById(Long passengerId) {
    return webClient
        .get()
        .uri("/" + passengerId)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException("Passenger service is not responding")))
        .toEntity(PassengerDto.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }

  public void updatePassengerAfterRide(Ride ride, Double averagePassengerRatingByPassengerId) {
    PassengerAfterRideDto passengerAfterRideDto =
        PassengerAfterRideDto.builder()
            .passengerRating(averagePassengerRatingByPassengerId)
            .rideCost(ride.getCost())
            .passengerBankCardId(ride.getPassengerBankCardId())
            .build();

    webClient
        .put()
        .uri("/after-ride/" + ride.getPassengerId())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(passengerAfterRideDto)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException("Passenger service is not responding")))
        .toEntity(Void.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }
}
