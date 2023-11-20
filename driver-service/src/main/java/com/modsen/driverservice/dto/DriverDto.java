package com.modsen.driverservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
public class DriverDto {

  private Long id;

  @NotBlank @Email private String email;

  @JsonProperty(access = WRITE_ONLY)
  @Size(min = 3, max = 30, message = "{driver.password.error}")
  @NotBlank
  private String password;

  @Size(min = 3, max = 30, message = "{driver.firstname.error}")
  @NotBlank
  private String firstName;

  @Size(min = 3, max = 30, message = "{driver.lastname.error}")
  @NotBlank
  private String lastName;

  @Min(value = 0, message = "{driver.rating.min-max.error}")
  @Max(value = 5, message = "{driver.rating.min-max.error}")
  private Double rating;

  private boolean isActive;
}
