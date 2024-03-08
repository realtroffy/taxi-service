package com.modsen.passengerservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassengerAfterRideDto {

  @NotNull
  @Positive
  private BigDecimal rideCost;
  private String passengerBankCardId;

  @DecimalMin(value = "0")
  @DecimalMax(value = "5.0")
  private Double passengerRating;
}
