package com.grupo8.turnos_app.modules.stats.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.modules.stats.dto.AdminStatsResponse;
import com.grupo8.turnos_app.modules.stats.dto.BusinessStatsResponse;
import com.grupo8.turnos_app.modules.stats.dto.EmployeeStatsResponse;
import com.grupo8.turnos_app.modules.stats.service.StatsService;
import com.grupo8.turnos_app.modules.users.entities.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    // GET /businesses/{id}/stats - owner only
    // period can be "day", "week", "month", "year" or custom date range with dateFrom and dateTo
    @GetMapping("/businesses/{businessId}/stats")
    public ResponseEntity<BusinessStatsResponse> getBusinessStats(
            @PathVariable UUID businessId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(statsService.getBusinessStats(businessId, period, dateFrom, dateTo, currentUser));
    }

    // GET /businesses/{id}/stats/employees/{employeeId} - owner or employee
    @GetMapping("/businesses/{businessId}/stats/employees/{employeeId}")
    public ResponseEntity<EmployeeStatsResponse> getEmployeeStats(
            @PathVariable UUID businessId,
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(statsService.getEmployeeStats(businessId, employeeId, period, dateFrom, dateTo, currentUser));
    }

    // GET /admin/stats - platform stats, user growth, etc - admin only
    @GetMapping("/admin/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(statsService.getAdminStats(period, dateFrom, dateTo));
    }
}
