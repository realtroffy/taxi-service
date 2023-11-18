package com.modsen.passengerservice.controller;

import com.modsen.passengerservice.dto.BankCardDto;
import com.modsen.passengerservice.service.BankCardService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/bankcards")
public class BankCardController {

  private final BankCardService bankCardService;

  @GetMapping("/{id}")
  public ResponseEntity<BankCardDto> getById(@PathVariable("id") long id) {
    return ResponseEntity.ok(bankCardService.getById(id));
  }

  @GetMapping
  public ResponseEntity<List<BankCardDto>> getAll(Pageable pageable) {
    List<BankCardDto> bankCards = bankCardService.getAll(pageable);
    return ResponseEntity.ok(bankCards);
  }

  @PostMapping
  public ResponseEntity<BankCardDto> save(@RequestBody @Valid BankCardDto bankCardDto) {
    return ResponseEntity.status(CREATED).body(bankCardService.save(bankCardDto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable("id") long id, @Valid @RequestBody BankCardDto bankCardDto) {
    bankCardService.update(id, bankCardDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    bankCardService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
