package com.grupo8.turnos_app.modules.users.dto;

import java.util.Set;

import com.grupo8.turnos_app.common.enums.RoleName;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleUpdateRequest {

    @NotEmpty
    private Set<RoleName> roles;
}