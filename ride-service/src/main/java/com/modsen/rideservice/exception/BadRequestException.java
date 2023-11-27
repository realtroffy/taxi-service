package com.modsen.rideservice.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException(String messageException) {
    super(messageException);
  }
}
