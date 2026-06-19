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
    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotBlank(message = "Client email is required")
    @Email(message = "Client email is not valid")
    private String clientEmail;

    @NotBlank(message = "Client phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Client phone is not valid")
    private String clientPhone;

}