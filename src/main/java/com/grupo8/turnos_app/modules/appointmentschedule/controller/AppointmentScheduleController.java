package com.grupo8.turnos_app.modules.appointmentschedule.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.modules.appointment.service.AppointmentGeneratorService;
import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleRequest;
import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleResponse;
import com.grupo8.turnos_app.modules.appointmentschedule.service.AppointmentScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/appointment-schedules")
@RequiredArgsConstructor
public class AppointmentScheduleController {

    private final AppointmentScheduleService appointmentScheduleService;
    private final AppointmentGeneratorService appointmentGeneratorService;

    @GetMapping
    public ResponseEntity<List<AppointmentScheduleResponse>> getByBusiness(@PathVariable UUID businessId) {
        return ResponseEntity.ok(appointmentScheduleService.getByBusiness(businessId));
    }

    @PostMapping
    public ResponseEntity<AppointmentScheduleResponse> create(
        @PathVariable UUID businessId,
        @Valid @RequestBody AppointmentScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentScheduleService.create(businessId, request));
    }

    @PostMapping("/generate")
    public ResponseEntity<Void> generateSlots(@PathVariable UUID businessId) {
        appointmentGeneratorService.generateSlotsForBusiness(businessId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        appointmentScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentScheduleResponse> update(
        @PathVariable UUID businessId,
        @PathVariable UUID id,
        @Valid @RequestBody AppointmentScheduleRequest request) {
        return ResponseEntity.ok(appointmentScheduleService.update(businessId, id, request));
    }
}