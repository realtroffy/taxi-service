package com.modsen.rideservice.exception;

public class FinishDateEarlyThanStartDateException extends IllegalArgumentException {

  public FinishDateEarlyThanStartDateException(String exceptionMessage) {
    super(exceptionMessage);
  }
}
