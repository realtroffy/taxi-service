package com.modsen.driverservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
public class CarDto {

  @NotNull @Positive private Long driverId;

  @NotBlank
  @Size(min = 1, max = 30, message = "{car.model.error}")
  private String model;

  @NotBlank
  @Size(min = 3, max = 30, message = "{car.colour.error}")
  private String colour;

  @NotBlank
  @Size(min = 5, max = 10, message = "{car.number.error}")
  private String number;
}
