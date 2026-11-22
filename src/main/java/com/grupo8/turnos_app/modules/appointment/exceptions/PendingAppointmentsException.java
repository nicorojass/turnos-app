package com.grupo8.turnos_app.modules.appointment.exceptions;

public class PendingAppointmentsException extends RuntimeException {
    public PendingAppointmentsException(String message) {
        super(message);
    }
}