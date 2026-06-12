package com.grupo8.turnos_app.modules.business.exceptions;

public class BusinessNotFoundException extends RuntimeException {

    public BusinessNotFoundException(String message) {
        super(message);
    }
}