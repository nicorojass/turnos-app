package com.grupo8.turnos_app.modules.appointmentschedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;

@Repository
public interface AppointmentScheduleRepository extends JpaRepository<AppointmentSchedule, Long> {
    List<AppointmentSchedule> findByBusinessId(Long businessId);
}