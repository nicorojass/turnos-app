package com.grupo8.turnos_app.modules.appointment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookAppointmentRequest {
    @NotBlank(message = "Ingresa tu nombre")
    private String clientName;

    @NotBlank(message = "Ingresa tu email ")
    @Email(message = "El email no es válido")
    private String clientEmail;

    @NotBlank(message = "Ingresa tu teléfono ")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "El teléfono no es válido")
    private String clientPhone;

}