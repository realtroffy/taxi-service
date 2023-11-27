package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.exception.BadRequestException;
import com.modsen.rideservice.exception.ServerUnavailableException;
import com.modsen.rideservice.service.WebClientService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static reactor.core.publisher.Mono.error;

@Service
@AllArgsConstructor
public class WebClientPassengerServiceUpdateAfterRideImpl implements WebClientService<Void> {

  private final WebClient webClient;

  @Override
  public ResponseEntity<Void> getResponseEntity(String bankCardUrl, Object body) {
    return webClient
        .put()
        .uri(bankCardUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(body)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            error -> error(new BadRequestException("Bad request for passenger service url")))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException("Passenger service is not responding")))
        .toEntity(Void.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }
}
