package com.grupo8.turnos_app.modules.deposit.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status;

    // timestamp deposit was paid (null until paid)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // the business's cancellation notice when the appointment was booked. cancelling uses this one,
    // so changing the policy later doesn't change the rule of appointments already booked.
    // deposits created before this column existed get 24, the rule they were booked with
    @Column(name = "cancellation_notice_hours")
    private Integer cancellationNoticeHours;

    @PrePersist
    protected void onCreate() {
        this.publicId = UUID.randomUUID();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;
}