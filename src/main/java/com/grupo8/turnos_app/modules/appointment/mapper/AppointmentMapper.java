package com.grupo8.turnos_app.modules.appointment.mapper;

import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.deposit.mapper.DepositMapper;

public class AppointmentMapper {

  public static AppointmentResponse toResponse(Appointment appointment) {
    if (appointment == null)
      return null;

    return AppointmentResponse.builder()
        .id(appointment.getPublicId())
        .startDatetime(appointment.getStartDatetime())
        .endDatetime(appointment.getEndDatetime())
        .status(appointment.getStatus())
        .clientName(appointment.getClientName())
        .clientEmail(appointment.getClientEmail())
        .clientPhone(appointment.getClientPhone())
        .price(appointment.getPrice())
        .businessId(appointment.getBusiness().getPublicId())
        .serviceId(appointment.getService().getPublicId())
        .serviceName(appointment.getService().getName())
        .employeeId(appointment.getEmployee() != null ? appointment.getEmployee().getPublicId() : null)
        .employeeName(appointment.getEmployee() != null ? appointment.getEmployee().getName() : null)
        .createdAt(appointment.getCreatedAt())
        .deposit(DepositMapper.toResponse(appointment.getDeposit()))
        .build();
  }
}