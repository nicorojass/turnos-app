package com.grupo8.turnos_app.modules.stats.dto;

import java.math.BigDecimal;

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
// DTO for service-level stats in business and employee stats responses
public class ServiceStatsDto {
    private String serviceName;
    private long appointmentCount;
    private BigDecimal revenue;
}
