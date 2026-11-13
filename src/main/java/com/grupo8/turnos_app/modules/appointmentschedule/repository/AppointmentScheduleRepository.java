package com.grupo8.turnos_app.modules.appointmentschedule.repository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;


public interface AppointmentScheduleRepository extends JpaRepository<AppointmentSchedule, Long> {
    List<AppointmentSchedule> findByBusinessId(Long businessId);
  @Query("""
    SELECT COUNT(a) > 0 FROM AppointmentSchedule a
    WHERE a.business.id = :businessId
    AND a.dayNumber = :dayNumber
    AND a.startTime < :endTime
    AND a.endTime > :startTime
    AND (
        (:employeeId IS NULL AND a.employee IS NULL)
        OR (a.employee.id = :employeeId)
    )
    AND (:excludeId IS NULL OR a.id <> :excludeId)
    """)
boolean existsConflictingSchedule(
    @Param("businessId") Long businessId,
    @Param("dayNumber") Integer dayNumber,
    @Param("startTime") LocalTime startTime,
    @Param("endTime") LocalTime endTime,
    @Param("employeeId") Long employeeId,
    @Param("excludeId") Long excludeId
);

    Optional<AppointmentSchedule> findByPublicId(UUID publicId);

    @Modifying
    @Query("DELETE FROM AppointmentSchedule a WHERE a.service.id = :serviceId")
    int deleteByServiceId(@Param("serviceId") Long serviceId);

    @Modifying
    @Query("UPDATE AppointmentSchedule a SET a.price = :price WHERE a.service.id = :serviceId")
    int updatePriceByServiceId(@Param("serviceId") Long serviceId, @Param("price") BigDecimal price);
}