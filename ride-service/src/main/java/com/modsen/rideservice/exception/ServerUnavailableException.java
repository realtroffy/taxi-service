package com.modsen.rideservice.exception;

public class ServerUnavailableException extends RuntimeException {

  public ServerUnavailableException(String messageException) {
    super(messageException);
  }
}
