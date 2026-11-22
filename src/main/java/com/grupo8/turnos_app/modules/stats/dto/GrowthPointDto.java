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
// DTO for user growth points in admin stats response
// year and month represent the time period, newUsers is the amount of new users
// a growth point is a month's new users created, used to generate growth charts in admin dashboard
public class GrowthPointDto {
    private int year;
    private int month;
    private long newUsers;
}
