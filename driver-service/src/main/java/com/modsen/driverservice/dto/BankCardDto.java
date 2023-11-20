package com.modsen.driverservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class BankCardDto {

  private Long id;
  @Positive @NotNull private Long driverId;

  @Digits(
      integer = 16,
      fraction = 0,
      message = "The number must be an integer and consist of 16 digits")
  @NotNull
  private Long cardNumber;

  @PositiveOrZero @NotNull private BigDecimal balance;

  @JsonProperty(value = "isDefault")
  private boolean isDefault;
}
