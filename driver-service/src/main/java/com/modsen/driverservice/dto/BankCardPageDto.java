package com.modsen.driverservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class BankCardPageDto {

  private List<BankCardDto> bankCardDtoList;
}
