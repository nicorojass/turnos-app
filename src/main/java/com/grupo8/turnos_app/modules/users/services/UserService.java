package com.grupo8.turnos_app.modules.users.services;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.modules.role.entity.Role;
import com.grupo8.turnos_app.modules.role.repository.RoleRepository;
import com.grupo8.turnos_app.modules.users.dto.UserResponse;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.mapper.UserMapper;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserResponse getUserById(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse toggleUserActive(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        user.setActive(!user.getActive());
        userRepository.save(user);
            return UserMapper.toResponse(user);
    }

    public void forceDeleteUser(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        userRepository.delete(user);
    }

    public UserResponse updateUserRoles(UUID publicId, Set<RoleName> roleNames) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(roles);
        return UserMapper.toResponse(userRepository.save(user));
    }
}
