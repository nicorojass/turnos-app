package com.grupo8.turnos_app.modules.users.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.modules.users.dto.RoleUpdateRequest;
import com.grupo8.turnos_app.modules.users.dto.UserResponse;
import com.grupo8.turnos_app.modules.users.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserResponse> updateUserRoles(@PathVariable Long id, @RequestBody @Valid RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserRoles(id, request.getRoles()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> toggleUserActive(@PathVariable Long id) {
        userService.toggleUserActive(id);
        return ResponseEntity.noContent().build();
    }
}