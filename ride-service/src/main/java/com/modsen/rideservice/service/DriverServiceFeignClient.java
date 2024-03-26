package com.modsen.rideservice.service;

import com.modsen.rideservice.dto.DriverWithCarDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "driver-service")
public interface DriverServiceFeignClient {

  @GetMapping("/api/v1/drivers/{id}")
  ResponseEntity<DriverWithCarDto> getById(@PathVariable("id") long id);
}
