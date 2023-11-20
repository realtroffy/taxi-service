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
  @Size(min = 1, max = 30, message = "Model should be between 1 and 30 characters")
  private String model;

  @NotBlank
  @Size(min = 3, max = 30, message = "Colour should be between 1 and 30 characters")
  private String colour;

  @NotBlank
  @Size(min = 5, max = 10, message = "Number should be between 1 and 30 characters")
  private String number;
}
