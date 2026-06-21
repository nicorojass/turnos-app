package com.grupo8.turnos_app.modules.stats.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// DTO for employee-level stats in employee stats response
public class EmployeeStatsResponse {
    private UUID employeeId;
    private String employeeName;
    private PeriodResponse period;
    private EmployeeAppointmentStatsDto appointments;
    // hours worked = sum of durationMinutes from finished appointments / 60
    private double hoursWorked;
    private BigDecimal revenueGenerated;
    private List<ServiceStatsDto> byService;
}
