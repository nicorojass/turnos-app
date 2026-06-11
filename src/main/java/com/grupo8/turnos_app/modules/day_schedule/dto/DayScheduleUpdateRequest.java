package com.grupo8.turnos_app.modules.day_schedule.dto;

import java.time.LocalTime;

import com.grupo8.turnos_app.common.enums.DayOfWeek;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class DayScheduleUpdateRequest {

    @NotNull(message = "Day is required")
    private DayOfWeek day;

    private LocalTime dayStart;

    private LocalTime dayEnd;

    @Min(value = 5, message = "Appointment duration must be at least 5 minutes")
    private Integer appointmentDuration;

    @NotNull(message = "Enabled is required")
    private Boolean enabled;
}