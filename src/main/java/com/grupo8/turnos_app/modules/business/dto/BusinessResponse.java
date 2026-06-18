package com.grupo8.turnos_app.modules.business.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class BusinessResponse {

    private UUID id;
    private String name;
    private String email;
    private String slug;
    private String phone;
    private String description;
    private Boolean automaticSchedule;
    private LocalDateTime scheduleEnd;
    private Integer scheduleDaysToCreate;
    private Integer scheduleAnticipation;
    private Boolean deleted;
    private UUID ownerId;
    private List<BusinessTypeResponse> businessTypes;
}