package com.modsen.driverservice.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.modsen.driverservice.exception.DriverWithoutCarAvailableException;
import com.modsen.driverservice.exception.RideSearchDtoMappingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(EmptyResultDataAccessException.class)
  public ResponseEntity<Object> handleEmptyResultDataAccessException() {
    String errorMessage = "The requested resource was not found.";
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<String> handleConstraintViolation() {
    return new ResponseEntity<>("Database Constraint Violation", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Object> handleNoSuchElementException(Exception ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler({DriverWithoutCarAvailableException.class, RideSearchDtoMappingException.class})
  public ResponseEntity<Object> handleCustomException(Exception ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    Throwable mostSpecificCause = ex.getMostSpecificCause();
    String errorMessage;
    if (mostSpecificCause instanceof InvalidFormatException) {
      InvalidFormatException ife = (InvalidFormatException) mostSpecificCause;
      String fieldName = ife.getPath().get(0).getFieldName();
      errorMessage = "Invalid value for field '" + fieldName + "'. Please provide a valid value.";
    } else {
      errorMessage = "Invalid request body. Please provide a valid request payload.";
    }
    return ResponseEntity.badRequest().body(errorMessage);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolationException() {
    return new ResponseEntity<>("Database Constraint Violation", HttpStatus.BAD_REQUEST);
  }
}
