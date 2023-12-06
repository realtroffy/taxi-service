package com.modsen.rideservice.dto;

import com.modsen.rideservice.model.Status;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
  private Long passengerBankCardId;
  private String promoCodeName;
  private BigDecimal cost;
  private Status status;
  private CarDto carDto;
}
