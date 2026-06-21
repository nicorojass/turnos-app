package com.grupo8.turnos_app.modules.users.services;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.common.exception.EmailAlreadyInUseException;
import com.grupo8.turnos_app.common.exception.ForbiddenOperationException;
import com.grupo8.turnos_app.common.exception.NotFoundException;
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

    public List<UserResponse> getEmployeesByBusiness(UUID businessId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));
        return business.getEmployees().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse createEmployee(UUID businessId, EmployeeRequest request, String ownerEmail) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Your account could not be found. Please try again."));

        if (!business.getOwner().getId().equals(owner.getId()))
            throw new ForbiddenOperationException("You don't have permission to add employees to this business.");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new EmailAlreadyInUseException("An account with that email address already exists. Please use a different one.");

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new NotFoundException("A required system role could not be found. Please contact support."));

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
            .orElseThrow(() -> new NotFoundException("We couldn't find the employee you're looking for."));

    if (employee.getRoles().stream().noneMatch(r -> r.getName() == RoleName.EMPLOYEE)) {
        throw new NotFoundException("Empleado no encontrado");
        }

        return UserMapper.toResponse(employee);
    }

    public UserResponse updateEmployee(UUID publicId, EmployeeRequest request) {
        User employee = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("We couldn't find the employee you're looking for."));
                
        if (employee.getRoles().stream().noneMatch(r -> r.getName() == RoleName.EMPLOYEE)) {
        throw new ForbiddenOperationException("The user you're trying to update is not registered as an employee.");
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
                .orElseThrow(() -> new NotFoundException("We couldn't find the employee you're looking for."));

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new NotFoundException("A required system role could not be found. Please contact support."));

        employee.getRoles().remove(employeeRole);
        userRepository.save(employee);

        List<Business> businesses = businessRepository.findByEmployees_Id(employee.getId());
        for (Business b : businesses) {
            b.getEmployees().remove(employee);
            businessRepository.save(b);
        }
    }
}