package com.grupo8.turnos_app.modules.appointment.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.dto.BookAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.service.AppointmentService;
import com.grupo8.turnos_app.modules.users.entities.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment and deposit management")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ---------- OWNER ENDPOINTS ------------

    // PUT /appointments/{id}/suspend - OWNER
    @PutMapping("/appointments/{id}/suspend")
    @Operation(summary = "Suspend appointment - deposit always refunded", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AppointmentResponse> suspendAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.suspendAppointment(id));
    }

    // DELETE /appointments/{id} - OWNER
    @DeleteMapping("/appointments/{id}")
    @Operation(summary = "Delete an UNBOOKED slot", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // GET /businesses/{id}/appointments - OWNER
    @GetMapping("/businesses/{businessId}/appointments")
    @Operation(summary = "Get all appointments for a business (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @PathVariable Long businessId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByBusiness(businessId, status, pageable));
    }

    // GET /businesses/{id}/appointments/today - OWNER
    @GetMapping("/businesses/{businessId}/appointments/today")
    @Operation(summary = "Get today's appointments for a business", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AppointmentResponse>> getTodayAppointments(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(appointmentService.getTodayAppointments(businessId));
    }

    // ------------ PUBLIC / ALL-ROLE ENDPOINTS ----------

    // PUT /appointments/{id}/cancel - OWNER / EMPLOYEE / CLIENT
    @PutMapping("/appointments/{id}/cancel")
    @Operation(summary = "Cancel appointment with deposit refund logic", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    // GET /businesses/{id}/appointments/public - PUBLIC
    @GetMapping("/businesses/{businessId}/appointments/public")
    @Operation(summary = "Get available slots for booking (public)")
    public ResponseEntity<List<AppointmentResponse>> getAvailableSlots(
            @PathVariable Long businessId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(
                appointmentService.getAvailableSlots(businessId, serviceId, employeeId));
    }

    // POST /appointments/{id}/book - PUBLIC
    @PostMapping("/appointments/{id}/book")
    @Operation(summary = "Book an appointment and create a deposit in PENDING status")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @PathVariable Long id,
            @RequestBody @Valid BookAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(id, request));
    }

    // POST /appointments/{id}/pay-deposit - PUBLIC
    @PostMapping("/appointments/{id}/pay-deposit")
    @Operation(summary = "Confirm deposit payment - appointment moves to BOOKED")
    public ResponseEntity<AppointmentResponse> confirmDepositPayment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.confirmDepositPayment(id));
    }

    // GET /users/me/appointments - CLIENT
    @GetMapping("/users/me/appointments")
    @Operation(summary = "Get my appointments as a registered client", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(authenticatedUser.getId()));
    }
}