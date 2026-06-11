package com.grupo8.turnos_app.modules.users.mapper;

import com.grupo8.turnos_app.modules.users.dto.UserResponse;
import com.grupo8.turnos_app.modules.users.entities.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .active(user.getActive())
                .build();
    }
}