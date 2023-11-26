package com.modsen.rideservice.exception;

public class AlreadyFinishedRideException extends RuntimeException {

  public AlreadyFinishedRideException(String message) {
    super(message);
  }
}
