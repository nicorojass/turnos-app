package com.grupo8.turnos_app.modules.appointment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.deposit.entity.Deposit;
import com.grupo8.turnos_app.modules.service.entity.Serv;
import com.grupo8.turnos_app.modules.users.entities.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "start_datetime", nullable = false)
  private LocalDateTime startDatetime;

  @Column(name = "end_datetime", nullable = false)
  private LocalDateTime endDatetime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AppointmentStatus status;

  // client data if who books the appointment isnt registered
  @Column(name = "client_name")
  private String clientName;

  @Column(name = "client_email")
  private String clientEmail;

  @Column(name = "client_phone")
  private String clientPhone;

  // service's price at booking moment, so it stays protected from price
  // variations on service
  @Column(nullable = false)
  private BigDecimal price;

  // appointment creation timestapm
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  // asign default values to timestamp and appt status, instead of doing it in
  // service
  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    if (this.status == null) {
      this.status = AppointmentStatus.UNBOOKED;
    }
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id", nullable = false)
  private Serv service;

  // assigned employee (can be null if not assigned)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  private User employee;

  // assigned client (can be null when not booked or booked by not registered
  // user)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_user_id")
  private User clientUser;

  @OneToOne(mappedBy = "appointment", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
  // cascade added only for delete appointment function, so associated deposit
  // also deletes
  private Deposit deposit;
}
