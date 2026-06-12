package com.grupo8.turnos_app.modules.appointmentschedule.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;


public interface AppointmentScheduleRepository extends JpaRepository<AppointmentSchedule, Long> {
    List<AppointmentSchedule> findByBusinessId(Long businessId);
    boolean existsByBusinessIdAndDayNumberAndStartTimeAndEndTime(
    Long businessId, Integer dayNumber, LocalTime startTime, LocalTime endTime);
    boolean existsByBusinessIdAndDayNumberAndStartTimeAndEndTimeAndIdNot(
    Long businessId, Integer dayNumber, LocalTime startTime, LocalTime endTime, Long id);
}