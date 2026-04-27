package com.grupo8.turnos_app.modules.service;

import java.math.BigDecimal;

import com.grupo8.turnos_app.modules.business.Business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="services")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Serv {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Positive
    private BigDecimal price;

    @Positive
    @Column(name="duration_minutes")
    private Integer durationMinutes;

    @ManyToOne
    private Business business;

}
