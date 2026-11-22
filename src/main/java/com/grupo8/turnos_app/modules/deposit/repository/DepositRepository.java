package com.grupo8.turnos_app.modules.deposit.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.modules.deposit.entity.Deposit;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    // get deposit by appointment id
    Optional<Deposit> findByAppointmentId(Long appointmentId);
    Optional<Deposit> findByPublicId(UUID publicId);

    // sum deposits by status for a business, filtered by appointment start date
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d " +
        "WHERE d.appointment.business.id = :businessId AND d.status = :status " +
        "AND d.appointment.startDatetime >= :from AND d.appointment.startDatetime < :to")
    BigDecimal sumByBusinessAndStatus(
        @Param("businessId") Long businessId,
        @Param("status") DepositStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    // sum deposits globally by status (admin)
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d " +
        "WHERE d.status = :status " +
        "AND d.appointment.startDatetime >= :from AND d.appointment.startDatetime < :to")
    BigDecimal sumByStatusGlobal(
        @Param("status") DepositStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}