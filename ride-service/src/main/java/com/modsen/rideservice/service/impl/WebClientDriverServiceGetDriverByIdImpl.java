package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.DriverWithCarDto;
import com.modsen.rideservice.exception.BadRequestException;
import com.modsen.rideservice.exception.ServerUnavailableException;
import com.modsen.rideservice.service.WebClientService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;

import static reactor.core.publisher.Mono.error;

@Service
@AllArgsConstructor
public class WebClientDriverServiceGetDriverByIdImpl implements WebClientService<DriverWithCarDto> {

  private final WebClient webClient;

  @Override
  public ResponseEntity<DriverWithCarDto> getResponseEntity(String url, Object body) {
    return webClient
        .get()
        .uri(url)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException("Driver service is not responding")))
        .toEntity(DriverWithCarDto.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }
}
