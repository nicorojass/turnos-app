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
// DTO for admin appointment stats in admin stats response
public class AdminAppointmentStatsDto {
    private long total;
    private long finished;
    private long cancelled;
    private long suspended;
}
