package com.grupo8.turnos_app.modules.business.exceptions;

public class BusinessAlreadyExistsException extends RuntimeException {
    public BusinessAlreadyExistsException(String message) {
        super(message);
    }

}
