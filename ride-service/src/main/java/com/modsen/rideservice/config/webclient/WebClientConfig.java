package com.modsen.rideservice.config.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${driver.service.url}")
    private String driverServiceUrl;
    @Value("${passenger.service.url}")
    private String passengerServiceUrl;

    @Bean
    @Profile("docker")
    public WebClient driverWebClient(){
        return WebClient.builder().baseUrl(driverServiceUrl).build();
    }

    @Bean
    @Profile("docker")
    public WebClient passengerWebClient(){
        return WebClient.builder().baseUrl(passengerServiceUrl).build();
    }
}
