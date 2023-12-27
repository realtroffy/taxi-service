package com.modsen.rideservice.dto;

import com.modsen.rideservice.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class RideDto {

  private Long id;
  @NotBlank
  private String startLocation;
  @NotBlank
  private String endLocation;
  @NotNull
  private Long passengerId;
  private Long driverId;
  private Integer driverRating;
  private Integer passengerRating;
  private LocalDateTime bookingTime;
  private LocalDateTime approvedTime;
  private LocalDateTime startTime;
  private LocalDateTime finishTime;
  private String passengerBankCardId;
  private String promoCodeName;
  private BigDecimal cost;
  private Status status;
  private CarDto carDto;
}
