package com.modsen.driverservice.exception;

public class DriverWithoutCarAvailableException extends RuntimeException {

  public DriverWithoutCarAvailableException(String message) {
    super(message);
  }
}
