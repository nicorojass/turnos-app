package com.grupo8.turnos_app.modules.day_schedule.exception;

public class InvalidScheduleTimeException extends RuntimeException {

    public InvalidScheduleTimeException(String message) {
        super(message);
    }
}