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

    @NotNull(message = "El día es obligatorio")
    @Min(value = 0, message = "El día debe estar entre 0 y 6")
    @Max(value = 6, message = "El día debe estar entre 0 y 6")
    private Integer dayNumber;

    @NotNull(message = "Ingresa la hora de inicio")
    private LocalTime startTime;

    @NotNull(message = "Ingresa la hora de finalización")
    private LocalTime endTime;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    private UUID employeeId;
}