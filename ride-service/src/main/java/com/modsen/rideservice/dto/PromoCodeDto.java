package com.modsen.rideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
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
