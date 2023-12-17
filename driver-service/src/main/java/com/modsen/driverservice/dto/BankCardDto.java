package com.modsen.driverservice.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class BankCardDto {

  private Long id;
  @Positive @NotNull private Long driverId;

  @Pattern(regexp = "[\\d]{16}", message = "{card.number.error}")
  @NotNull
  private String cardNumber;

  @PositiveOrZero @NotNull private BigDecimal balance;

  private boolean isDefault;
}
