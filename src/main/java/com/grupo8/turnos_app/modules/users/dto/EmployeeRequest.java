package com.grupo8.turnos_app.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @Size(min = 6)
    private String password;
}