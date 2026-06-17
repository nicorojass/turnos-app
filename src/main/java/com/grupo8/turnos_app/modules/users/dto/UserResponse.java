package com.grupo8.turnos_app.modules.users.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.grupo8.turnos_app.common.enums.RoleName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private Boolean active;
    private Set<RoleName> roles;
}