package com.grupo8.turnos_app.modules.appointmentschedule.exception;

public class InvalidScheduleRangeException extends RuntimeException{
    public InvalidScheduleRangeException(String message) {
        super(message);
    }
}
