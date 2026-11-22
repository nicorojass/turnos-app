package com.grupo8.turnos_app.modules.appointmentschedule.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

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
public class AppointmentScheduleResponse {

    private UUID id;
    private Integer dayNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private UUID businessId;
    private UUID serviceId;
    private UUID employeeId;
}
