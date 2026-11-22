package com.grupo8.turnos_app.modules.users.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.common.exception.ConflictException;
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

    public UserResponse toggleUserActive(UUID publicId, User currentUser) {
    User user = userRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

    // an admin deactivating itself, or the last active admin, would leave the platform without one
    if (Boolean.TRUE.equals(user.getActive())) {
        if (user.getId().equals(currentUser.getId()))
            throw new ConflictException("No podés desactivar tu propia cuenta");
        if (isAdmin(user) && userRepository.countByActiveTrueAndRoles_Name(RoleName.ADMIN) <= 1)
            throw new ConflictException("No se puede desactivar al último administrador");
    }

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

        // deactivating an owner deletes its business, so reactivating it brings the business back.
        // before, the owner came back unable to edit it and unable to create a new one
        boolean isOwner = user.getRoles().stream()
                .anyMatch(r -> RoleName.OWNER.equals(r.getName()));
        if (isOwner) {
            businessRepository.findByOwnerId(user.getId()).ifPresent(business -> {
                if (Boolean.TRUE.equals(business.getDeleted())) {
                    business.setDeleted(false);
                    businessRepository.save(business);
                }
            });
        }
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

    public UserResponse updateUserRoles(UUID publicId, Set<RoleName> roleNames, User currentUser) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // same reason as toggleUserActive: the only admin could remove its own admin role
        if (user.getId().equals(currentUser.getId()))
            throw new ConflictException("No podés cambiar tus propios roles");
        if (isAdmin(user) && !roleNames.contains(RoleName.ADMIN) && Boolean.TRUE.equals(user.getActive())
                && userRepository.countByActiveTrueAndRoles_Name(RoleName.ADMIN) <= 1)
            throw new ConflictException("No se puede quitar el rol de administrador al último administrador");

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new NotFoundException("Rol no encontrado: " )))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(roles);
        return UserMapper.toResponse(userRepository.save(user));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN);
    }
}
