package com.grupo8.turnos_app.modules.business.exceptions;

public class BusinessTypeAlreadyAssignedException extends RuntimeException {
    public BusinessTypeAlreadyAssignedException(String message) {
        super(message);
    }

}
