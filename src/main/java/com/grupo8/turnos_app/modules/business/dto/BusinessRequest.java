package com.grupo8.turnos_app.modules.business.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Ingresar el nombre del negocio")
    private String name;
    
    @NotBlank(message = "Ingresar el email del negocio")
    @Email(message = "Formato de email inválido")
    private String email;
    
    @NotBlank(message = "Ingresa tu link personalizado")
    private String slug;
    
    @NotBlank(message = "Ingresar el teléfono del negocio")
    @Pattern(regexp = "^\\+[0-9]{6,20}$", message = "Phone format must start with + followed by digits")
    private String phone;
    
    private String description;
    
    private Boolean automaticSchedule;
    
    private LocalDateTime scheduleEnd;
    
    private Integer scheduleDaysToCreate;
    
    private Integer scheduleAnticipation;
    
    private List<Long> typeIds;
}