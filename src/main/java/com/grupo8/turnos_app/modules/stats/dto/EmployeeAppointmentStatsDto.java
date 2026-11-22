package com.grupo8.turnos_app.modules.stats.dto;

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
// DTO for employee appointment stats in employee stats response
public class EmployeeAppointmentStatsDto {
    private long total;
    private long finished;
    private long upcoming;
    private long cancelled;
    private double occupancyRate;
}
