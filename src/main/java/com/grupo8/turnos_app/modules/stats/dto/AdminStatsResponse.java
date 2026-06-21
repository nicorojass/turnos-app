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
// DTO for admin-level stats response.
// contains overall stats for the platform in the given period, such as total businesses, active businesses, total users, new users, appointment stats, revenue stats, top businesses and user growth points
public class AdminStatsResponse {
    private PeriodResponse period;
    private long totalBusinesses;
    private long activeBusinessesInPeriod;
    private long totalUsers;
    private long newUsersInPeriod;
    private AdminAppointmentStatsDto appointments;
    private RevenueStatsDto revenue;
    private List<BusinessSummaryDto> topBusinesses;
    private List<GrowthPointDto> userGrowth;
}
