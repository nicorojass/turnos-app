
package com.grupo8.turnos_app.modules.appointment.repository;

import java.math.BigDecimal;
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
    "AND ((a.status = 'BOOKED' AND a.endDatetime > :now) OR a.status = 'AWAITING_PAYMENT')")
  boolean hasPendingAppointmentsByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

  @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.business.id = :businessId " +
    "AND ((a.status = 'BOOKED' AND a.endDatetime > :now) OR a.status = 'AWAITING_PAYMENT')")
  boolean hasPendingAppointmentsByBusiness(@Param("businessId") Long businessId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.clientUser.id = :userId " +
    "AND a.status IN ('BOOKED', 'AWAITING_PAYMENT') " +
    "AND a.startDatetime < :endDatetime AND a.endDatetime > :startDatetime")
  boolean hasOverlappingAppointment(
    @Param("userId") Long userId,
    @Param("startDatetime") LocalDateTime startDatetime,
    @Param("endDatetime") LocalDateTime endDatetime);

  @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.clientEmail = :email " +
    "AND a.status = 'AWAITING_PAYMENT'")
  boolean hasUnpaidByEmail(@Param("email") String email);

  @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.clientUser.id = :userId " +
    "AND a.status = 'AWAITING_PAYMENT'")
  boolean hasUnpaidByUser(@Param("userId") Long userId);

  @Query("SELECT a FROM Appointment a WHERE a.status = 'AWAITING_PAYMENT' " +
    "AND a.reservedAt < :expiry")
  List<Appointment> findExpiredReservations(@Param("expiry") LocalDateTime expiry);

  // ---- stats queries ----

  // count by status for a business in a date range
  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.status = :status AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countByBusinessAndStatus(
    @Param("businessId") Long businessId,
    @Param("status") com.grupo8.turnos_app.common.enums.AppointmentStatus status,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);

  // finished = booked and past
  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countFinishedByBusiness(
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // upcoming = booked and future
  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.endDatetime >= :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countUpcomingByBusiness(
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // revenue from finished appointments
  @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  BigDecimal sumRevenueFinishedByBusiness(
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // breakdown by service: [serviceName, count, sumPrice]
  @Query("SELECT a.service.name, COUNT(a), COALESCE(SUM(a.price), 0) " +
    "FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to " +
    "GROUP BY a.service.name ORDER BY COUNT(a) DESC")
  List<Object[]> statsGroupedByService(
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // breakdown by employee: [employeePublicId, employeeName, count, sumPrice]
  @Query("SELECT a.employee.publicId, a.employee.name, COUNT(a), COALESCE(SUM(a.price), 0) " +
    "FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.employee IS NOT NULL AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to " +
    "GROUP BY a.employee.publicId, a.employee.name ORDER BY COUNT(a) DESC")
  List<Object[]> statsGroupedByEmployee(
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // peak days: [dayOfWeek int (mysql: 1=sun..7=sat), count]
  @Query("SELECT FUNCTION('DAYOFWEEK', a.startDatetime), COUNT(a) " +
    "FROM Appointment a WHERE a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.startDatetime >= :from AND a.startDatetime < :to " +
    "GROUP BY FUNCTION('DAYOFWEEK', a.startDatetime) ORDER BY COUNT(a) DESC")
  List<Object[]> statsGroupedByDayOfWeek(
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);

  // unique registered clients that had a finished appointment
  @Query("SELECT COUNT(DISTINCT a.clientUser.id) FROM Appointment a " +
    "WHERE a.business.id = :businessId AND a.clientUser IS NOT NULL " +
    "AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countUniqueRegisteredClients(
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // ---- employee-specific stats ----

  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.employee.id = :employeeId " +
    "AND a.business.id = :businessId " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countByEmployee(
    @Param("employeeId") Long employeeId,
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);

  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.employee.id = :employeeId " +
    "AND a.business.id = :businessId AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countFinishedByEmployee(
    @Param("employeeId") Long employeeId,
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.employee.id = :employeeId " +
    "AND a.business.id = :businessId AND a.status = 'BOOKED' AND a.endDatetime >= :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countUpcomingByEmployee(
    @Param("employeeId") Long employeeId,
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.employee.id = :employeeId " +
    "AND a.business.id = :businessId AND a.status = 'CANCELLED' " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countCancelledByEmployee(
    @Param("employeeId") Long employeeId,
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);

  // sum service duration minutes for hours-worked calc
  @Query("SELECT COALESCE(SUM(a.service.durationMinutes), 0) FROM Appointment a " +
    "WHERE a.employee.id = :employeeId AND a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long sumMinutesWorkedByEmployee(
    @Param("employeeId") Long employeeId,
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a " +
    "WHERE a.employee.id = :employeeId AND a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  BigDecimal sumRevenueByEmployee(
    @Param("employeeId") Long employeeId,
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // [serviceName, count] for employee breakdown
  @Query("SELECT a.service.name, COUNT(a) FROM Appointment a " +
    "WHERE a.employee.id = :employeeId AND a.business.id = :businessId " +
    "AND a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to " +
    "GROUP BY a.service.name ORDER BY COUNT(a) DESC")
  List<Object[]> statsGroupedByServiceForEmployee(
    @Param("employeeId") Long employeeId,
    @Param("businessId") Long businessId,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // ---- admin global stats ----

  @Query("SELECT COUNT(a) FROM Appointment a " +
    "WHERE a.startDatetime >= :from AND a.startDatetime < :to")
  long countAllInRange(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);

  @Query("SELECT COUNT(a) FROM Appointment a " +
    "WHERE a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countAllFinishedInRange(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  @Query("SELECT COUNT(a) FROM Appointment a " +
    "WHERE a.status = 'CANCELLED' AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countAllCancelledInRange(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);

  @Query("SELECT COUNT(a) FROM Appointment a " +
    "WHERE a.status = 'SUSPENDED' AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countAllSuspendedInRange(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);

  @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a " +
    "WHERE a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to")
  BigDecimal sumAllRevenueInRange(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now);

  // [businessName, count, sumPrice] top businesses
  @Query("SELECT a.business.name, COUNT(a), COALESCE(SUM(a.price), 0) " +
    "FROM Appointment a WHERE a.status = 'BOOKED' AND a.endDatetime < :now " +
    "AND a.startDatetime >= :from AND a.startDatetime < :to " +
    "GROUP BY a.business.name ORDER BY COUNT(a) DESC")
  List<Object[]> topBusinessesByAppointments(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    @Param("now") LocalDateTime now,
    Pageable pageable);

  // count distinct businesses that had at least one booked appointment in period
  @Query("SELECT COUNT(DISTINCT a.business.id) FROM Appointment a " +
    "WHERE a.status = 'BOOKED' AND a.startDatetime >= :from AND a.startDatetime < :to")
  long countActiveBusinessesInRange(
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to);
}