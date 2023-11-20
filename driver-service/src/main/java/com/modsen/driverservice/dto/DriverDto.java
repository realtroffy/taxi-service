package com.modsen.driverservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modsen.driverservice.model.Car;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
public class DriverDto {

  private Long id;

  @NotBlank @Email private String email;

  @JsonProperty(access = WRITE_ONLY)
  @Size(min = 3, max = 30, message = "Password should be between 3 and 30 characters")
  @NotBlank
  private String password;

  @Size(min = 3, max = 30, message = "First name should be between 3 and 30 characters")
  @NotBlank
  private String firstName;

  @Size(min = 3, max = 30, message = "First name should be between 3 and 30 characters")
  @NotBlank
  private String lastName;

  @JsonProperty(access = READ_ONLY)
  private Double rating;

  private boolean isActive;

  private List<BankCardDto> bankCards = new ArrayList<>();

  private Car car;
}
