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
// DTO for appointment stats in business and employee stats responses
// contains total appointments, finished, upcoming, cancelled, suspended, occupancy rate and cancellation rate
public class AppointmentStatsDto {
    private long total;
    private long finished;
    private long upcoming;
    private long cancelled;
    private long suspended;
    private double occupancyRate;
    private double cancellationRate;
}
