
package com.grupo8.turnos_app.modules.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;

import jakarta.persistence.LockModeType;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

  // pessimistic lock to prevent double booking on concurrent reqs
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM Appointment a WHERE a.id = :id")
  Optional<Appointment> findByIdWithLock(@Param("id") Long id);

  // get all appointments by businessId. pageable and filterable by status
  Page<Appointment> findByBusinessIdAndStatus(Long businessId, AppointmentStatus status, Pageable pageable);

  // get all appointments businessId by without status filter
  Page<Appointment> findByBusinessId(Long businessId, Pageable pageable);

  // get todays appointments by businessid. ordered by start time
  @Query("SELECT a FROM Appointment a WHERE a.business.id = :businessId " +
      "AND a.startDatetime >= :startOfDay AND a.startDatetime < :endOfDay " +
      "ORDER BY a.startDatetime ASC")
  List<Appointment> findTodayAppointments(
      @Param("businessId") Long businessId,
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay);

  // get future slots for the public booking view, with optional filters
  @Query("SELECT a FROM Appointment a WHERE a.business.id = :businessId " +
      "AND a.status = 'UNBOOKED' AND a.startDatetime > :now " +
      "AND (:serviceId IS NULL OR a.service.id = :serviceId) " +
      "AND (:employeeId IS NULL OR a.employee.id = :employeeId) " +
      "ORDER BY a.startDatetime ASC")
  List<Appointment> findAvailableSlots(
      @Param("businessId") Long businessId,
      @Param("now") LocalDateTime now,
      @Param("serviceId") Long serviceId,
      @Param("employeeId") Long employeeId);

  // get all appointments by client as registered user
  List<Appointment> findByClientUserIdOrderByStartDatetimeDesc(Long clientUserId);

  @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.service.id = :serviceId " +
    "AND ((:employeeId IS NULL AND a.employee IS NULL) OR a.employee.id = :employeeId) " +
    "AND a.startDatetime = :startDatetime")
    boolean existsSlot(
    @Param("businessId") Long businessId,
    @Param("serviceId") Long serviceId,
    @Param("employeeId") Long employeeId,
    @Param("startDatetime") LocalDateTime startDatetime);

  Optional<Appointment> findByPublicId(UUID publicId);

  @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.clientUser.id = :userId " +
    "AND a.status IN ('BOOKED', 'AWAITING_PAYMENT')")
    boolean hasPendingAppointmentsByUser(@Param("userId") Long userId);

  @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.status IN ('BOOKED', 'AWAITING_PAYMENT')")
    boolean hasPendingAppointmentsByBusiness(@Param("businessId") Long businessId);
}