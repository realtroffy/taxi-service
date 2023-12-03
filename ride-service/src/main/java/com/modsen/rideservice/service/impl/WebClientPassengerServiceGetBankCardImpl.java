package com.modsen.rideservice.service.impl;

import com.modsen.rideservice.dto.BankCardDto;
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
public class WebClientPassengerServiceGetBankCardImpl implements WebClientService<BankCardDto> {

  private final WebClient webClient;

  @Override
  public ResponseEntity<BankCardDto> getResponseEntity(String passengerUpdateUrl, Object body) {
    return webClient
        .get()
        .uri(passengerUpdateUrl)
        .retrieve()
        .onStatus(
            HttpStatus::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new NoSuchElementException(error))))
        .onStatus(
            HttpStatus::is5xxServerError,
            error -> error(new ServerUnavailableException("Passenger service is not responding")))
        .toEntity(BankCardDto.class)
        .timeout(Duration.ofMinutes(1))
        .block();
  }
}
