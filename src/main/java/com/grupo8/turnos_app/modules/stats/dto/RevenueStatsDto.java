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
// DTO for revenue details 
public class RevenueStatsDto {
    private BigDecimal totalFromFinished;
    private BigDecimal depositsCollected;
    private BigDecimal depositsRefunded;
    private BigDecimal depositsForfeited;
}
