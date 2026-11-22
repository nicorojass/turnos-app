package com.grupo8.turnos_app.modules.appointment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.deposit.repository.DepositRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentExpirationJob {

    // how long an unpaid booking holds the slot. also used for expiresAt in AppointmentResponse
    public static final int RESERVATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final DepositRepository depositRepository;

    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime expiry = LocalDateTime.now().minusMinutes(RESERVATION_MINUTES);
        List<Appointment> expired = appointmentRepository.findExpiredReservations(expiry);

        for (Appointment appointment : expired) {
            appointment.setStatus(AppointmentStatus.UNBOOKED);
            appointment.setClientName(null);
            appointment.setClientEmail(null);
            appointment.setClientPhone(null);
            appointment.setClientUser(null);
            appointment.setReservedAt(null);

            depositRepository.findByAppointmentId(appointment.getId()).ifPresent(deposit -> {
                deposit.setStatus(DepositStatus.CANCELED);
                depositRepository.save(deposit);
            });

            appointmentRepository.save(appointment);
        }
    }
}