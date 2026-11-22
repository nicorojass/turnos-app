package com.grupo8.turnos_app.modules.appointment.exceptions;

public class InvalidPriceException extends RuntimeException {
  public InvalidPriceException(String message) {
    super(message);
  }
}
