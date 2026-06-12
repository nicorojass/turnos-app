package com.grupo8.turnos_app.common.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentNotAvailableException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidStatusException;
import com.grupo8.turnos_app.modules.appointmentschedule.exception.AppointmentScheduleAlreadyExistsException;
import com.grupo8.turnos_app.modules.appointmentschedule.exception.InvalidScheduleRangeException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessAlreadyExistsException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessTypeAlreadyAssignedException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessTypeNotFoundException;
import com.grupo8.turnos_app.modules.business.exceptions.OwnerNotFoundException;
import com.grupo8.turnos_app.modules.day_schedule.exception.DayScheduleNotFoundException;
import com.grupo8.turnos_app.modules.day_schedule.exception.InvalidScheduleTimeException;
import com.grupo8.turnos_app.modules.service.exception.ServiceAlreadyExistsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, "Malformed request body or invalid field format"));
    }
    
     @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, ex.getMessage()));
    }

     @ExceptionHandler(InvalidScheduleRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidScheduleRangeException(InvalidScheduleRangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, ex.getMessage()));
    }

    @ExceptionHandler(InvalidScheduleTimeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidScheduleTime(InvalidScheduleTimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(401, "Invalid email or password"));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(403, ex.getMessage()));
    }

    @ExceptionHandler(DayScheduleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDayScheduleNotFound(DayScheduleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBusinessNotFound(BusinessNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
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
    
    @ExceptionHandler(BusinessTypeAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleBusinessTypeAlreadyAssigned(BusinessTypeAlreadyAssignedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(BusinessAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBusinessAlreadyExists(BusinessAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(ServiceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleServiceAlreadyExists(ServiceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }
    
    @ExceptionHandler(AppointmentScheduleAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentScheduleAlreadyExists(AppointmentScheduleAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailInUse(EmailAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal server error"));
    }

    @ExceptionHandler(AppointmentNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentNotAvailable(AppointmentNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

   

}