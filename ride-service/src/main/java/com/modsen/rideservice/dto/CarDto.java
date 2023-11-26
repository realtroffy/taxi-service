package com.modsen.rideservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarDto {

  private Long driverId;
  private String model;
  private String colour;
  private String number;
}
