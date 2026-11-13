package com.grupo8.turnos_app.common.exception;

import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentConflictException;
import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentNotAvailableException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidAppointmentException;
import com.grupo8.turnos_app.modules.appointment.exceptions.TooManyBookingsException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidStatusException;
import com.grupo8.turnos_app.modules.appointment.exceptions.PendingAppointmentsException;
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
@Slf4j
public class GlobalExceptionHandler {

    public static final String ACCOUNT_DISABLED_MESSAGE = "Esta cuenta está desactivada. Contactá al administrador.";

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, ex.getMessage()));
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

    @ExceptionHandler(InvalidAppointmentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAppointment(InvalidAppointmentException ex) {
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

    @ExceptionHandler(PendingAppointmentsException.class)
    public ResponseEntity<ErrorResponse> handlePendingAppointments(PendingAppointmentsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, ex.getMessage())); 
    }   

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentConflict(AppointmentConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyAttempts(TooManyAttemptsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ErrorResponse(429, ex.getMessage()));
    }

    @ExceptionHandler(TooManyBookingsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyBookings(TooManyBookingsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ErrorResponse(429, ex.getMessage()));
    }
    
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailInUse(EmailAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, ex.getMessage()));
    }
    
    // anything without its own handler: logged with the stack trace so a reported error can be traced
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal server error"));
    }

    // spring security checks the account before AuthService.login gets to, so a deactivated account
    // never reached the message there and got a 500
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledAccount(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, ACCOUNT_DISABLED_MESSAGE));
    }

    // two requests waiting for the same row lock: the one that loses gets a clear answer instead of a 500.
    // in practice this is two people booking the same slot at the same time
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrency(ConcurrencyFailureException ex, HttpServletRequest request) {
        log.warn("Concurrent update on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        String message = request.getRequestURI().endsWith("/book")
                ? "El turno ya no está disponible."
                : "Otra persona modificó estos datos al mismo tiempo. Probá de nuevo.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, message));
    }

    // a unique or foreign key constraint of the database: the services check the usual cases first,
    // this is the safety net for the rest
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation on {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Ya existe un registro con esos datos"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, ex.getMessage()));
    }

    // a path or query value of the wrong type: a malformed id, an unknown status, a malformed date
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "El valor de '" + ex.getName() + "' no es válido"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Falta el parámetro '" + ex.getParameterName() + "'"));
    }

    // @Valid on a list (the week of day schedules) is checked by method validation, not by
    // MethodArgumentNotValidException
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException ex) {
        String message = ex.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse(415, "El cuerpo del pedido tiene que ser JSON"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(405, "Método no permitido para esta ruta"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "La ruta no existe"));
    }

    @ExceptionHandler(AppointmentNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentNotAvailable(AppointmentNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, ex.getMessage()));
    }

   

}