package com.grupo8.turnos_app.modules.users.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.common.exception.EmailAlreadyInUseException;
import com.grupo8.turnos_app.common.exception.ForbiddenOperationException;
import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.appointment.exceptions.PendingAppointmentsException;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.role.entity.Role;
import com.grupo8.turnos_app.modules.role.repository.RoleRepository;
import com.grupo8.turnos_app.modules.users.dto.EmployeeRequest;
import com.grupo8.turnos_app.modules.users.dto.UserResponse;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.mapper.UserMapper;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    public List<UserResponse> getEmployeesByBusiness(UUID businessId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
        return business.getEmployees().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse createEmployee(UUID businessId, EmployeeRequest request, String ownerEmail) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Dueño no encontrado"));

        if (!business.getOwner().getId().equals(owner.getId()))
            throw new ForbiddenOperationException("No tenes permisos para agregar empleados a este negocio");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new EmailAlreadyInUseException("Ya existe una cuenta con este email");

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE role not found"));

        User employee = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        employee.getRoles().add(employeeRole);
        User savedEmployee = userRepository.save(employee);

        business.getEmployees().add(savedEmployee);
        businessRepository.save(business);

        return UserMapper.toResponse(savedEmployee);
    }

    public UserResponse getEmployee(UUID publicId) {
    User employee = userRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

    if (employee.getRoles().stream().noneMatch(r -> r.getName() == RoleName.EMPLOYEE)) {
        throw new NotFoundException("Empleado no encontrado");
        }

        return UserMapper.toResponse(employee);
    }

    public UserResponse updateEmployee(UUID publicId, EmployeeRequest request) {
        User employee = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));
                
        if (employee.getRoles().stream().noneMatch(r -> r.getName() == RoleName.EMPLOYEE)) {
        throw new ForbiddenOperationException("El usuario a modificar no es un empleado");
        }
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return UserMapper.toResponse(userRepository.save(employee));
    }

    public void removeEmployee(UUID publicId) {
        User employee = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        if (appointmentRepository.hasPendingAppointmentsByEmployee(employee.getId(), LocalDateTime.now())) {
            throw new PendingAppointmentsException("Employee has pending appointments and cannot be removed");
        }

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE role not found"));

        employee.getRoles().remove(employeeRole);
        userRepository.save(employee);

        List<Business> businesses = businessRepository.findByEmployees_Id(employee.getId());
        for (Business b : businesses) {
            b.getEmployees().remove(employee);
            businessRepository.save(b);
        }
    }
}