package com.grupo8.turnos_app.modules.day_schedule.dto;

import java.time.LocalTime;

import com.grupo8.turnos_app.common.enums.DayOfWeek;

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
public class DayScheduleResponse {
    private Long id;
    private DayOfWeek day;
    private LocalTime dayStart;
    private LocalTime dayEnd;
    private Integer appointmentDuration;
    private Boolean enabled;
    private Long businessId;
}
