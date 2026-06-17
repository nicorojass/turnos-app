package com.grupo8.turnos_app.modules.appointmentschedule.mapper;

import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleRequest;
import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleResponse;
import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;

public class AppointmentScheduleMapper {

    public static AppointmentScheduleResponse toResponse(AppointmentSchedule schedule) {
        if (schedule == null) return null;
        return AppointmentScheduleResponse.builder()
                .id(schedule.getPublicId())
                .dayNumber(schedule.getDayNumber())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .price(schedule.getPrice())
                .businessId(schedule.getBusiness().getId())
                .serviceId(schedule.getService().getId())
                .employeeId(schedule.getEmployee() != null ? schedule.getEmployee().getId() : null)
                .build();
    }

    public static AppointmentSchedule toEntity(AppointmentScheduleRequest request) {
        return AppointmentSchedule.builder()
                .dayNumber(request.getDayNumber())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
    }
}