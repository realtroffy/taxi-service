package com.modsen.rideservice.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.modsen.rideservice.exception.AlreadyFinishedRideException;
import com.modsen.rideservice.exception.BadRequestException;
import com.modsen.rideservice.exception.DriverServiceAvailableDriversException;
import com.modsen.rideservice.exception.FinishDateEarlyThanStartDateException;
import com.modsen.rideservice.exception.PassengerBankCardNotEnoughMoneyException;
import com.modsen.rideservice.exception.PassengerBankCardNullException;
import com.modsen.rideservice.exception.ServerUnavailableException;
import com.modsen.rideservice.exception.UnfinishedBookingRideException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
  public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
    StringBuilder errorMessage = new StringBuilder("Bad data: ");
    Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
    for (ConstraintViolation<?> violation : violations) {
      errorMessage.append(violation.getMessage()).append(". ");
    }

    return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Object> handleNoSuchElementException(Exception ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler({
    BadRequestException.class,
    ServerUnavailableException.class,
    UnfinishedBookingRideException.class,
    DriverServiceAvailableDriversException.class,
    FinishDateEarlyThanStartDateException.class,
    PassengerBankCardNullException.class,
    PassengerBankCardNotEnoughMoneyException.class,
    AlreadyFinishedRideException.class
  })
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
  public ResponseEntity<Object> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    return ResponseEntity.badRequest().body(ex.getMostSpecificCause().getMessage());
  }
}
