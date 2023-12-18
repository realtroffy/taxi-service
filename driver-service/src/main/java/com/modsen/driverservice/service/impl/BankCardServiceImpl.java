package com.modsen.driverservice.service.impl;

import com.modsen.driverservice.dto.BankCardDto;
import com.modsen.driverservice.mapper.BankCardMapper;
import com.modsen.driverservice.model.BankCard;
import com.modsen.driverservice.repository.BankCardRepository;
import com.modsen.driverservice.service.BankCardService;
import com.modsen.driverservice.service.DriverService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor(onConstructor_ = {@Lazy})
public class BankCardServiceImpl implements BankCardService {

    private static final String NO_SUCH_BANK_CARD_EXCEPTION_MESSAGE =
            "Bank card was not found by id = ";
    private final BankCardMapper bankCardMapper;
    private final BankCardRepository bankCardRepository;
    @Lazy
    private final DriverService driverService;

    @Override
    @Transactional(readOnly = true)
    public BankCardDto getById(long id) {
        BankCard bankCard = getBankCard(id);
        return bankCardMapper.toDto(bankCard);
    }

    private BankCard getBankCard(long id) {
        return bankCardRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(NO_SUCH_BANK_CARD_EXCEPTION_MESSAGE + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankCardDto> getAll(Pageable pageable) {
        List<BankCardDto> bankCards = new ArrayList<>();
        bankCardRepository.findAll(pageable).forEach(bankCard -> bankCards.add(bankCardMapper.toDto(bankCard)));
        return bankCards;
    }

    @Override
    @Transactional
    public BankCardDto save(BankCardDto bankCardDto) {
        driverService.getById(bankCardDto.getDriverId());
        BankCard bankCard = bankCardMapper.toEntity(bankCardDto);
        BankCard createdBankCard = bankCardRepository.save(bankCard);
        return bankCardMapper.toDto(createdBankCard);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        getBankCard(id);
        bankCardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void update(long id, BankCardDto bankCardDto) {
        getBankCard(id);
        bankCardDto.setId(id);
        BankCard bankCard = bankCardMapper.toEntity(bankCardDto);
        bankCardRepository.save(bankCard);
    }
}
