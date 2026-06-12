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

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must not contain numbers")
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])[\\x00-\\x7F]+$", message = "Password must contain uppercase, lowercase, a number and only standard characters")
    private String password;
}