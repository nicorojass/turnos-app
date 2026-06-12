package com.grupo8.turnos_app.modules.appointment.exceptions;

public class AppointmentNotAvailableException extends RuntimeException {
    public AppointmentNotAvailableException(String message) {
        super(message);
    }
}
