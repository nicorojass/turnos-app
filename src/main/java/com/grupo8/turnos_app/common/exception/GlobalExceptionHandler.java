package com.grupo8.turnos_app.common.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.grupo8.turnos_app.modules.business.exceptions.BusinessAlreadyExistsException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessTypeAlreadyAssignedException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessTypeNotFoundException;
import com.grupo8.turnos_app.modules.business.exceptions.OwnerNotFoundException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBusinessAlreadyExists(BusinessAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBusinessNotFound(BusinessNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(BusinessTypeAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleBusinessTypeAlreadyAssigned(BusinessTypeAlreadyAssignedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(BusinessTypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBusinessTypeNotFound(BusinessTypeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(OwnerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOwnerNotFound(OwnerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal server error"));
    }


}