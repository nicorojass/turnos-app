package com.grupo8.turnos_app.modules.deposit.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.modules.deposit.entity.Deposit;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    // get deposit by appointment id
    Optional<Deposit> findByAppointmentId(Long appointmentId);
    Optional<Deposit> findByPublicId(UUID publicId);
}