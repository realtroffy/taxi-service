package com.modsen.passengerservice.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class PassengerRideDto {

  @NotNull private Long passengerId;

  @PositiveOrZero @NotNull private BigDecimal amountRide;

  @Min(value = 0, message = "Rating should be between 0 and 5")
  @Max(value = 5, message = "Rating should be between 0 and 5")
  @NotNull
  private Double ratingFromDriver;

  private boolean isCardPayed;
}
