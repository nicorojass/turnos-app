package com.grupo8.turnos_app.modules.appointment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.dto.BookAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.service.AppointmentService;
import com.grupo8.turnos_app.modules.users.entities.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ---------- OWNER ENDPOINTS ------------

    // PUT /appointments/{id}/suspend - OWNER
    @PutMapping("/appointments/{id}/suspend")
    public ResponseEntity<AppointmentResponse> suspendAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.suspendAppointment(id));
    }
    
    // PUT /appointments/{id}/complete - OWNER
    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    // DELETE /appointments/{id} - OWNER
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // GET /businesses/{id}/appointments - OWNER
    @GetMapping("/businesses/{businessId}/appointments")
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @PathVariable UUID businessId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByBusiness(businessId, status, pageable));
    }

    // GET /businesses/{id}/appointments/today - OWNER
    @GetMapping("/businesses/{businessId}/appointments/today")
    public ResponseEntity<List<AppointmentResponse>> getTodayAppointments(
            @PathVariable UUID businessId) {
        return ResponseEntity.ok(appointmentService.getTodayAppointments(businessId));
    }

    // ------------ PUBLIC / ALL-ROLE ENDPOINTS ----------

    // PUT /appointments/{id}/cancel - OWNER / EMPLOYEE / CLIENT
    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    // GET /businesses/{id}/appointments/public - PUBLIC
    @GetMapping("/businesses/{businessId}/appointments/public")
    public ResponseEntity<List<AppointmentResponse>> getAvailableSlots(
            @PathVariable UUID businessId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(
                appointmentService.getAvailableSlots(businessId, serviceId, employeeId));
    }

    // POST /appointments/{id}/book - PUBLIC
    @PostMapping("/appointments/{id}/book")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @PathVariable UUID id,
            @RequestBody @Valid BookAppointmentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.bookAppointment(id, request, authentication));
    }

    // POST /appointments/{id}/pay-deposit - PUBLIC
    @PostMapping("/appointments/{id}/pay-deposit")
    public ResponseEntity<AppointmentResponse> confirmDepositPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.confirmDepositPayment(id));
    }

    // GET /users/me/appointments - CLIENT
    @GetMapping("/users/me/appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(authenticatedUser.getId()));
    }
}