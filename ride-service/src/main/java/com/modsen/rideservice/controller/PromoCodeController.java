package com.modsen.rideservice.controller;

import com.modsen.rideservice.dto.PromoCodeDto;
import com.modsen.rideservice.dto.PromoCodePageDto;
import com.modsen.rideservice.service.PromoCodeService;
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

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/promocodes")
@RolesAllowed("ADMIN")
public class PromoCodeController {

  private final PromoCodeService promoCodeService;

  @GetMapping("/{id}")
  public ResponseEntity<PromoCodeDto> getById(@PathVariable("id") long id) {
    return ResponseEntity.ok(promoCodeService.getById(id));
  }

  @GetMapping
  public ResponseEntity<PromoCodePageDto> getAll(Pageable pageable) {
    List<PromoCodeDto> promoCodeDtoList = promoCodeService.getAll(pageable);
    PromoCodePageDto promoCodePageDto =
        PromoCodePageDto.builder().promoCodeDtoList(promoCodeDtoList).build();
    return ResponseEntity.ok(promoCodePageDto);
  }

  @PostMapping
  public ResponseEntity<PromoCodeDto> save(@RequestBody @Valid PromoCodeDto promoCodeDto) {
    PromoCodeDto savedPromoCode = promoCodeService.save(promoCodeDto);
    return ResponseEntity.status(CREATED).body(savedPromoCode);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable("id") long id, @Valid @RequestBody PromoCodeDto promoCodeDto) {
    promoCodeService.update(id, promoCodeDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") long id) {
    promoCodeService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
