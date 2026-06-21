package com.grupo8.turnos_app.modules.users.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.common.exception.EmailAlreadyInUseException;
import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.appointment.exceptions.PendingAppointmentsException;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
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
    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    
    public UserResponse getUserById(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse toggleUserActive(UUID publicId) {
    User user = userRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

    if (Boolean.TRUE.equals(user.getActive())) {
        if (appointmentRepository.hasPendingAppointmentsByUser(user.getId(), LocalDateTime.now()))
            throw new PendingAppointmentsException("El usuario tiene turnos pendientes y no puede ser desactivado");

        boolean isOwner = user.getRoles().stream()
                .anyMatch(r -> RoleName.OWNER.equals(r.getName()));

        if (isOwner) {
            businessRepository.findByOwnerId(user.getId()).ifPresent(business -> {
                if (appointmentRepository.hasPendingAppointmentsByBusiness(business.getId(), LocalDateTime.now()))
                    throw new PendingAppointmentsException("El usuario tiene turnos pendientes y no puede ser desactivado");
                business.setDeleted(true);
                businessRepository.save(business);
            });
        }
    } else {
        if (userRepository.existsByEmailAndActiveTrue(user.getEmail()))
            throw new EmailAlreadyInUseException("Ya existe una cuenta activa con este email");
    }

    user.setActive(!user.getActive());
    userRepository.save(user);
    return UserMapper.toResponse(user);
    }
    
    public void forceDeleteUser(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (appointmentRepository.hasPendingAppointmentsByUser(user.getId(), LocalDateTime.now())) {
        throw new PendingAppointmentsException("El usuario tiene turnos pendientes y no puede ser eliminado");
    }
        userRepository.delete(user);
    }

    public UserResponse updateUserRoles(UUID publicId, Set<RoleName> roleNames) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new NotFoundException("Rol no encontrado: " )))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(roles);
        return UserMapper.toResponse(userRepository.save(user));
    }
}
