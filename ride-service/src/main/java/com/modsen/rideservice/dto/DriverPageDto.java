package com.modsen.rideservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DriverPageDto {

  private List<DriverDto> driverDtoList;
}
