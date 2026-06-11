package com.grupo8.turnos_app.modules.appointmentschedule;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentScheduleRepository extends JpaRepository<AppointmentSchedule, Long> {
    List<AppointmentSchedule> findByBusinessId(Long businessId);
}