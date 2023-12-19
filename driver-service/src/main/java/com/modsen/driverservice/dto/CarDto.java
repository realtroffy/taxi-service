package com.modsen.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
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
