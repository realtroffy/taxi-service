package com.modsen.rideservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverDto {

  private Long id;
  private String firstName;
  private String lastName;
  private CarDto carDto;
}
