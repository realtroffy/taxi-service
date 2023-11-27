package com.modsen.rideservice.exception;

public class PassengerBankCardNotEnoughMoneyException extends RuntimeException {

  public PassengerBankCardNotEnoughMoneyException(String message) {
    super(message);
  }
}
