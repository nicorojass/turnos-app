package com.grupo8.turnos_app.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Ingresa el nombre del empleado")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "El nombre del empleado solo puede contener letras y espacios")
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank(message = "Ingresa la contraseña del empleado")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])[\\x00-\\x7F]+$", message = "La contraseña debe contener mayúsculas, minúsculas, un número y no puede contener caracteres especiales")
    private String password;
}