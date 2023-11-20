package com.modsen.driverservice.controller;

import com.modsen.driverservice.dto.CarDto;
import com.modsen.driverservice.service.CarService;
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
@RequestMapping("/api/v1/cars")
public class CarController {

    private final CarService carService;

    @GetMapping("/{id}")
    public ResponseEntity<CarDto> getById(@PathVariable("id") long id) {
        return ResponseEntity.ok(carService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CarDto>> getAll(Pageable pageable) {
        List<CarDto> cars = carService.getAll(pageable);
        return ResponseEntity.ok(cars);
    }

    @PostMapping
    public ResponseEntity<CarDto> save(@RequestBody @Valid CarDto carDto) {
        return ResponseEntity.status(CREATED).body(carService.save(carDto));
    }

    @PutMapping
    public ResponseEntity<Void> update(@Valid @RequestBody CarDto carDto) {
        carService.update(carDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        carService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
