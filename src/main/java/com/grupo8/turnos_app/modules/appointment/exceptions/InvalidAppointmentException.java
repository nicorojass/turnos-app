package com.grupo8.turnos_app.modules.appointment.exceptions;

// data that doesn't make sense for a new appointment, like a start that already passed or
// client data without a name. it's answered with a 400
public class InvalidAppointmentException extends RuntimeException {
    public InvalidAppointmentException(String message) {
        super(message);
    }
}
