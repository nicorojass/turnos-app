package com.grupo8.turnos_app.modules.day_schedule.mapper;

import com.grupo8.turnos_app.modules.day_schedule.dto.DayScheduleResponse;
import com.grupo8.turnos_app.modules.day_schedule.entity.DaySchedule;

public class DayScheduleMapper {
  
  public static DayScheduleResponse toResponse(DaySchedule daySchedule) {
    return DayScheduleResponse.builder()
        .id(daySchedule.getId())
        .day(daySchedule.getDay())
        .dayStart(daySchedule.getDayStart())
        .dayEnd(daySchedule.getDayEnd())
        .appointmentDuration(daySchedule.getAppointmentDuration())
        .enabled(daySchedule.getEnabled())
        .businessId(daySchedule.getBusiness().getId())
        .build();
  }
}
