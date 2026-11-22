package com.grupo8.turnos_app.modules.stats.dto;

import java.time.LocalDate;

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
// DTO for representing a date range period in stats requests and responses
public class PeriodResponse {
    private LocalDate from;
    private LocalDate to;
}
