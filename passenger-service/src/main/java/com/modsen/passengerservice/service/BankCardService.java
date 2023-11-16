package com.modsen.passengerservice.service;

import com.modsen.passengerservice.dto.BankCardDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BankCardService {

    BankCardDto getById(long id);

    List<BankCardDto> getAll(Pageable pageable);

    BankCardDto save(BankCardDto bankCardDto);

    void deleteById(long id);

    void update(long id, BankCardDto bankCardDto);
}
