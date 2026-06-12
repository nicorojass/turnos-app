package com.grupo8.turnos_app.modules.appointmentschedule.exception;

public class AppointmentScheduleAlreadyExistsException extends RuntimeException {
    public AppointmentScheduleAlreadyExistsException(String message) {
        super(message);
    }
}
