package com.grupo8.turnos_app.modules.appointment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// what the owner sends from the agenda in the panel to create one appointment on a specific day
// and time. the end is not sent, it comes from the service duration: a 45 min haircut at 10:00
// ends at 10:45.
// the client fields are all optional. without them the appointment is created free (UNBOOKED)
// and shows up on the public page; with them it's booked for that person right away, for
// example somebody who asked for an appointment over the phone. in that case at least the name
// is needed, the email and phone are optional because not everybody gives them
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateAppointmentRequest {

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private LocalDateTime startDatetime;

    @NotNull(message = "El servicio es obligatorio")
    private UUID serviceId;

    // null means not assigned to anybody in the team
    private UUID employeeId;

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String clientName;

    @Email(message = "El email no es válido")
    @Size(max = 255, message = "El email no puede superar los 255 caracteres")
    private String clientEmail;

    // same rule as the public booking (7 to 20 characters, digits, spaces, + or -), but here an
    // empty text is accepted too, because the panel form sends the field empty when the owner
    // leaves it blank, and the service treats it as not sent
    @Pattern(regexp = "^$|^[0-9+\\-\\s]{7,20}$", message = "Ingresa un telefono valido")
    private String clientPhone;
}
