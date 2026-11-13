package com.grupo8.turnos_app.common.exception;

// the request is valid but goes against the current state of the data (answers 409)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
