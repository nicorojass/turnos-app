package com.grupo8.turnos_app.modules.business.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessRequest {

    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Slug is required")
    private String slug;
    
    @NotBlank(message = "Phone is required")
    private String phone;
    
    private String description;
    
    private Boolean automaticSchedule;
    
    private LocalDateTime scheduleEnd;
    
    private Integer scheduleDaysToCreate;
    
    private Integer scheduleAnticipation;
    
    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    private List<Long> typeIds;
}