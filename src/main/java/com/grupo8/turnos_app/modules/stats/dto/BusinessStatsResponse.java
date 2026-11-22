package com.grupo8.turnos_app.modules.stats.dto;

import java.util.List;

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
// DTO for business-level stats response. 
// contains period, appointment stats, revenue stats, service-level stats, employee-level stats, peak days and unique clients count
// each field is represented by its own DTO class to keep the response structured adn complete
public class BusinessStatsResponse {
    private PeriodResponse period;
    private AppointmentStatsDto appointments;
    private RevenueStatsDto revenue;
    private List<ServiceStatsDto> byService;
    private List<EmployeeSummaryDto> byEmployee;
    private List<DayStatsDto> peakDays;
    private long uniqueClientsCount;
}
