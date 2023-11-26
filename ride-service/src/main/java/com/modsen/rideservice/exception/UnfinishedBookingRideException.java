package com.modsen.rideservice.exception;

public class UnfinishedBookingRideException extends RuntimeException {

  public UnfinishedBookingRideException(String messageException) {
    super(messageException);
  }
}
