package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.dto.DriverRatingDto;
import com.modsen.rideservice.dto.DriverWithCarDto;
import com.modsen.rideservice.dto.IdPageDto;
import com.modsen.rideservice.exception.ServerUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static reactor.core.publisher.Mono.error;

@Component
public class DriverServiceWebClient {

  public static final String SERVER_UNAVAILABLE_EXCEPTION_MESSAGE =
      "Driver service is not responding";
  public static final String PREFIX_BEARER = "Bearer ";
  public static final String HEADER_AUTHORIZATION = "Authorization";

  private final WebClient webClient;
  @Value("${webclient.timeout.duration}")
  private long timeOutDuration;

  public DriverServiceWebClient(@Qualifier("driverWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  public ResponseEntity<DriverPageDto> getDriverPageDtoByListIdsDriver(List<Long> driversIdList) {

    IdPageDto idPageDto = IdPageDto.builder().listId(driversIdList).build();

    return webClient
        .post()
        .uri("/list-id")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .header(HEADER_AUTHORIZATION, PREFIX_BEARER + getJwt())
        .bodyValue(idPageDto)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(DriverPageDto.class)
        .timeout(Duration.ofMinutes(timeOutDuration))
        .block();
  }

  public void updateDriverAvailabilityToTrueAfterRide(Long driverId) {
    webClient
        .put()
        .uri("/" + driverId + "/available-true")
        .header(HEADER_AUTHORIZATION, PREFIX_BEARER + getJwt())
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(Void.class)
        .timeout(Duration.ofMinutes(timeOutDuration))
        .block();
  }

  public void updateDriverRatingAfterRide(Long driverId, DriverRatingDto driverRatingDto) {
    webClient
        .put()
        .uri("/" + driverId + "/new-rating")
        .header(HEADER_AUTHORIZATION, PREFIX_BEARER + getJwt())
        .bodyValue(driverRatingDto)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(Void.class)
        .timeout(Duration.ofMinutes(timeOutDuration))
        .block();
  }

  public ResponseEntity<DriverWithCarDto> getDriverById(Long driverId) {
    return webClient
        .get()
        .uri("/" + driverId)
        .header(HEADER_AUTHORIZATION, PREFIX_BEARER + getJwt())
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException(SERVER_UNAVAILABLE_EXCEPTION_MESSAGE)))
        .toEntity(DriverWithCarDto.class)
        .timeout(Duration.ofMinutes(timeOutDuration))
        .block();
  }

  private String getJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return jwt.getTokenValue();
  }
}
