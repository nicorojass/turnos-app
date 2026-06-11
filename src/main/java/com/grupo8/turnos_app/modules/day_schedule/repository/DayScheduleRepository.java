package com.grupo8.turnos_app.modules.day_schedule.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.common.enums.DayOfWeek;
import com.grupo8.turnos_app.modules.day_schedule.entity.DaySchedule;

public interface DayScheduleRepository extends JpaRepository<DaySchedule, Long> {

    List<DaySchedule> findAllByBusinessId(Long businessId);

    Optional<DaySchedule> findByBusinessIdAndDay(Long businessId, DayOfWeek day);
}