package com.modsen.rideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class DriverRatingDto {

    @Min(value = 0, message = "{driver.rating.min-max.error}")
    @Max(value = 5, message = "{driver.rating.min-max.error}")
    private Double rating;
}
