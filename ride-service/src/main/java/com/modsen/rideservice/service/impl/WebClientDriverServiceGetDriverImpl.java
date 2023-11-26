package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.DriverPageDto;
import com.modsen.rideservice.exception.BadRequestException;
import com.modsen.rideservice.exception.ServerUnavailableException;
import com.modsen.rideservice.service.WebClientService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static reactor.core.publisher.Mono.error;

@Service
@AllArgsConstructor
public class WebClientDriverServiceGetDriverImpl implements WebClientService<DriverPageDto> {

  private final WebClient webClient;

  @Override
  public ResponseEntity<DriverPageDto> getResponseEntity(String availableDriverUrl, Object body) {
    return webClient
        .put()
        .uri(availableDriverUrl)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            error -> error(new BadRequestException("Bad request for driver service url")))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException("Driver service is not responding")))
        .toEntity(DriverPageDto.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }
}
