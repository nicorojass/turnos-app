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
// DTO for day-level stats in business stats response, used to represent peak days
public class DayStatsDto {
    private String dayOfWeek;
    private long appointmentCount;
}
