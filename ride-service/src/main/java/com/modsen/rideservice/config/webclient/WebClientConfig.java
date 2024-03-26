package com.modsen.rideservice.config.webclient;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

  @Value("${driver.service.url}")
  private String driverServiceUrl;

  @Value("${passenger.service.url}")
  private String passengerServiceUrl;

  private final ReactorLoadBalancerExchangeFilterFunction filter;

  @Bean
  @Profile("!test")
  public WebClient driverWebClient() {
    return WebClient.builder()
        .filter(new ServletBearerExchangeFilterFunction())
        .filter(filter)
        .baseUrl(driverServiceUrl)
        .build();
  }

  @Bean
  @Profile("!test")
  public WebClient passengerWebClient() {
    return WebClient.builder()
        .filter(new ServletBearerExchangeFilterFunction())
        .filter(filter)
        .baseUrl(passengerServiceUrl)
        .build();
  }
}
