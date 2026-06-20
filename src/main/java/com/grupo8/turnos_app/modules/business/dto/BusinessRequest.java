package com.grupo8.turnos_app.modules.business.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @NotBlank(message = "Ingresar el email del negocio")
    @Email(message = "Formato de email inválido")
    @Size(max = 255, message = "El email no puede superar los 255 caracteres")
    private String email;

    @NotBlank(message = "Ingresa tu link personalizado")
    @Size(max = 100, message = "El link personalizado no puede superar los 100 caracteres")
    private String slug;

    @NotBlank(message = "Ingresar el teléfono del negocio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Teléfono inválido. Ingresá el número con código de país sin el signo +, por ejemplo: 541145678901")
    private String phone;

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    private String description;

    private Boolean automaticSchedule;

    private LocalDateTime scheduleEnd;

    @Min(value = 1, message = "El rango de días para crear el horario debe ser al menos 1")
    @Max(value = 180, message = "El rango de días para crear el horario no puede superar los 180 días")
    private Integer scheduleDaysToCreate;

    @Min(value = 0, message = "La anticipación no puede ser negativa")
    @Max(value = 60, message = "La anticipación no puede superar los 60 días")
    private Integer scheduleAnticipation;
    
    private List<Long> typeIds;
}