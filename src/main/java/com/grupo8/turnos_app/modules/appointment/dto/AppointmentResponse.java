package com.grupo8.turnos_app.modules.appointment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.modules.deposit.dto.DepositResponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private UUID id;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private AppointmentStatus status;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private BigDecimal price;
    private Long businessId;
    private Long serviceId;
    private String serviceName;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime createdAt;
    // can be null: its gonna be included when it exists
    private DepositResponse deposit;
}