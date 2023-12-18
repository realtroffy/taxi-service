package com.modsen.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class BankCardDto {

  private Long id;
  @Positive @NotNull private Long driverId;

  @Pattern(regexp = "[\\d]{16}", message = "{card.number.error}")
  @NotNull
  private String cardNumber;

  @PositiveOrZero @NotNull private BigDecimal balance;

  private boolean isDefault;
}
