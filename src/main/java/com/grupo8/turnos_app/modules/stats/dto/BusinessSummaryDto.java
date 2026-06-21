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
// DTO for business-level summary in business stats response, used to represent overall business performance in the given period
public class BusinessSummaryDto {
    private String businessName;
    private long appointmentCount;
    private BigDecimal revenue;
}
