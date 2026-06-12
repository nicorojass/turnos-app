package com.grupo8.turnos_app.modules.deposit.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "deposits")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status;

    // timestamp deposit was paid (null until paid)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;
}