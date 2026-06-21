package com.grupo8.turnos_app.modules.stats.dto;

import java.math.BigDecimal;
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
// DTO for employee-level stats in business stats response
public class EmployeeSummaryDto {
    private UUID employeeId;
    private String employeeName;
    private long appointmentCount;
    private BigDecimal revenue;
}
