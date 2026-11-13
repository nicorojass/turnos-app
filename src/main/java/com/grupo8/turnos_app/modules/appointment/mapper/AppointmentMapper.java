package com.grupo8.turnos_app.modules.appointment.mapper;

import java.time.temporal.ChronoUnit;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.service.AppointmentExpirationJob;
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
        .businessName(appointment.getBusiness().getName())
        .businessSlug(appointment.getBusiness().getSlug())
        .serviceId(appointment.getService().getPublicId())
        .serviceName(appointment.getService().getName())
        .employeeId(appointment.getEmployee() != null ? appointment.getEmployee().getPublicId() : null)
        .employeeName(appointment.getEmployee() != null ? appointment.getEmployee().getName() : null)
        .createdAt(appointment.getCreatedAt())
        .expiresAt(appointment.getStatus() == AppointmentStatus.AWAITING_PAYMENT && appointment.getReservedAt() != null
            ? appointment.getReservedAt().plusMinutes(AppointmentExpirationJob.RESERVATION_MINUTES).truncatedTo(ChronoUnit.SECONDS)
            : null)
        .deposit(DepositMapper.toResponse(appointment.getDeposit()))
        .build();
  }
}