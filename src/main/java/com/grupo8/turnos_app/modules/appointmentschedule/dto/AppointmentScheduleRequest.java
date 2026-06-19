package com.grupo8.turnos_app.modules.appointmentschedule.dto;

import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentScheduleRequest {

    @NotNull(message = "Day number is required")
    @Min(value = 0, message = "Day number must be between 0 and 6")
    @Max(value = 6, message = "Day number must be between 0 and 6")
    private Integer dayNumber;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    private UUID employeeId;
}