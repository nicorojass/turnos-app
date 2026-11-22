package com.grupo8.turnos_app.modules.service.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.grupo8.turnos_app.modules.business.entities.Business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;

    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name="deposit_porcentage", precision = 5, scale = 2)
    private BigDecimal depositPorcentage;

    @Positive
    @Min(1) @Max(480)
    @Column(name="duration_minutes")
    private Integer durationMinutes;

    @PrePersist
    protected void onCreate() {
        this.publicId = UUID.randomUUID();
    }

    @ManyToOne
    @JoinColumn(name="business_id", nullable=false)
    private Business business;

    private Boolean deleted;
}
