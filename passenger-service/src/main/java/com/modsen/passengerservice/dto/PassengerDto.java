package com.modsen.passengerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
public class PassengerDto {

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

  @Min(value = 0, message = "Rating should be between 0 and 5")
  @Max(value = 5, message = "Rating should be between 0 and 5")
  private Double rating;

  List<BankCardDto> bankCards = new ArrayList<>();
}
