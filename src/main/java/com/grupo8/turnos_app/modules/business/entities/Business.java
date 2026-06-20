package com.grupo8.turnos_app.modules.business.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.grupo8.turnos_app.modules.users.entities.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "businesses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 300)
    private String description;

    @Builder.Default
    private Boolean automaticSchedule = false;

    private LocalDateTime scheduleEnd;

    @Min(1) @Max(180) // validation for a reasonable range of days to create schedule
    @Builder.Default
    private Integer scheduleDaysToCreate = 30;

    @Min(0) @Max(60) // validation for a reasonable anticipation days to renew schedule
    @Builder.Default
    private Integer scheduleAnticipation = 5;

    private Boolean deleted;

    @PrePersist
    protected void OnCreate() {
        this.deleted = false;
        this.publicId = UUID.randomUUID();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "business_business_types",
        joinColumns = @JoinColumn(name = "business_id"),
        inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<BusinessType> businessTypes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "business_employees",
        joinColumns = @JoinColumn(name = "business_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> employees = new ArrayList<>();
}