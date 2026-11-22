package com.grupo8.turnos_app.modules.appointment.exceptions;

public class AppointmentConflictException extends RuntimeException {
    public AppointmentConflictException(String message) {
        super(message);
    }
}
