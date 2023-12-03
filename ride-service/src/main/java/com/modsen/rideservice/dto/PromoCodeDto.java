package com.modsen.rideservice.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromoCodeDto {

  private Long id;

  @NotNull private String name;

  @DecimalMin(value = "0.1")
  @DecimalMax(value = "0.9")
  @NotNull
  private BigDecimal discount;

  @NotNull private LocalDateTime start;

  @NotNull private LocalDateTime end;

  List<RideDto> rideDtoList;
}
