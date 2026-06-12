package com.grupo8.turnos_app.modules.day_schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.modules.day_schedule.dto.DayScheduleResponse;
import com.grupo8.turnos_app.modules.day_schedule.dto.DayScheduleUpdateRequest;
import com.grupo8.turnos_app.modules.day_schedule.service.DayScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/schedule")
@RequiredArgsConstructor
public class DayScheduleController {

    private final DayScheduleService dayScheduleService;

    // Returns whole week's schedule settings per day
    @GetMapping
    public ResponseEntity<List<DayScheduleResponse>> getSchedule(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(dayScheduleService.getScheduleByBusiness(businessId));
    }

    // Req exxpects a list of days to modify, not the whole week
    @PutMapping
    public ResponseEntity<List<DayScheduleResponse>> updateFullSchedule(
            @PathVariable Long businessId,
            @RequestBody @Valid List<DayScheduleUpdateRequest> requests) {
        return ResponseEntity.ok(dayScheduleService.updateFullSchedule(businessId, requests));
    }
}