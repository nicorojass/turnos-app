package com.grupo8.turnos_app.modules.appointment.exceptions;

public class TooManyBookingsException extends RuntimeException {
    public TooManyBookingsException(String message) {
        super(message);
    }
}
