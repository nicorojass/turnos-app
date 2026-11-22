package com.grupo8.turnos_app.modules.business.dto;

import jakarta.validation.constraints.NotBlank;
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
public class BusinessTypeRequest {

    @NotBlank(message = "Ingresa un rubro de negocio")
    @Size(max = 100, message = "El rubro no puede superar los 100 caracteres")
    private String name;
}