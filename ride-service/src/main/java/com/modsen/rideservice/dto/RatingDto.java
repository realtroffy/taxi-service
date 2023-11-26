package com.modsen.rideservice.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class RatingDto {

  private Long id;

  @Min(value = 0, message = "{rating.min-max.error}")
  @Max(value = 5, message = "{rating.min-max.error}")
  private Integer driverRating;

  @Min(value = 0, message = "{rating.min-max.error}")
  @Max(value = 5, message = "{rating.min-max.error}")
  private Integer passengerRating;
}
