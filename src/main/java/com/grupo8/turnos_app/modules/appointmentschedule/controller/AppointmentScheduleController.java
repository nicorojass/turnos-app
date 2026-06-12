package com.grupo8.turnos_app.modules.appointmentschedule.controller;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<AppointmentScheduleResponse>> getByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(appointmentScheduleService.getByBusiness(businessId));
    }

    @PostMapping
    public ResponseEntity<AppointmentScheduleResponse> create(
        @PathVariable Long businessId,
        @Valid @RequestBody AppointmentScheduleRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(appointmentScheduleService.create(businessId, request));
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentScheduleResponse> update(
        @PathVariable Long businessId,
        @PathVariable Long id,
        @Valid @RequestBody AppointmentScheduleRequest request) { 
    return ResponseEntity.ok(appointmentScheduleService.update(businessId, id, request));
}
}