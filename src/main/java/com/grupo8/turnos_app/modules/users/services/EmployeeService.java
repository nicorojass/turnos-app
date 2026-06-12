package com.grupo8.turnos_app.modules.users.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.modules.business.entities.Business;
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

    public List<UserResponse> getEmployeesByBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return business.getEmployees().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse createEmployee(Long businessId, EmployeeRequest request, String ownerEmail) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!business.getOwner().getId().equals(owner.getId()))
            throw new RuntimeException("You are not the owner of this business");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already in use");

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("EMPLOYEE role not found"));

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

    public UserResponse getEmployee(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public UserResponse updateEmployee(Long id, EmployeeRequest request) {
        User employee = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return UserMapper.toResponse(userRepository.save(employee));
    }

    public void removeEmployee(Long id) {
        User employee = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("EMPLOYEE role not found"));

        employee.getRoles().remove(employeeRole);
        userRepository.save(employee);

        List<Business> businesses = businessRepository.findByEmployees_Id(id);
        for (Business b : businesses) {
            b.getEmployees().remove(employee);
            businessRepository.save(b);
        }
    }
}