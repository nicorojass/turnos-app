package com.grupo8.turnos_app.modules.users.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.modules.users.dto.EmployeeRequest;
import com.grupo8.turnos_app.modules.users.dto.UserResponse;
import com.grupo8.turnos_app.modules.users.services.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/businesses/{businessId}/employees")
    public ResponseEntity<List<UserResponse>> getEmployees(@PathVariable Long businessId) {
        return ResponseEntity.ok(employeeService.getEmployeesByBusiness(businessId));
    }

    @PostMapping("/businesses/{businessId}/employees")
    public ResponseEntity<UserResponse> createEmployee(
            @PathVariable Long businessId,
            @RequestBody @Valid EmployeeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(businessId, request, userDetails.getUsername()));
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<UserResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<UserResponse> updateEmployee(@PathVariable Long id,
            @RequestBody @Valid EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> removeEmployee(@PathVariable Long id) {
        employeeService.removeEmployee(id);
        return ResponseEntity.noContent().build();
    }
}