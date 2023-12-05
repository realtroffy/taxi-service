package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.dto.DriverWithCarDto;
import com.modsen.rideservice.dto.IdPageDto;
import com.modsen.rideservice.exception.ServerUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static reactor.core.publisher.Mono.error;

@Component
public class DriverServiceWebClient {

  public static final String SERVER_UNAVAILABLE_EXCEPTION_MESSAGE =
      "Driver service is not responding";
  private final WebClient webClient;

  public DriverServiceWebClient(@Value("${driver.service.url}") String driverServiceUrl) {
    this.webClient = WebClient.builder().baseUrl(driverServiceUrl).build();
  }

  public ResponseEntity<DriverPageDto> getDriverPageDtoByListIdsDriver(List<Long> driversIdList) {

    IdPageDto idPageDto = IdPageDto.builder().listId(driversIdList).build();

    return webClient
        .post()
        .uri("/list-id")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(idPageDto)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(DriverPageDto.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }

  public void updateDriverAvailabilityToTrueAfterRide(Long driverId) {
    webClient
        .put()
        .uri("/" + driverId + "/available-true")
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(Void.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }

  public void updateDriverRatingAfterRide(Long driverId, Double averageDriverRatingByDriverId) {
    webClient
        .put()
        .uri("/" + driverId + "/" + averageDriverRatingByDriverId)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(Void.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }

  public ResponseEntity<DriverWithCarDto> getDriverById(Long driverId) {
    return webClient
        .get()
        .uri("/" + driverId)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(DriverWithCarDto.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }
}
