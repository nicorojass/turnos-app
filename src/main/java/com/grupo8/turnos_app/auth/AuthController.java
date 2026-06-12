package com.grupo8.turnos_app.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.auth.dto.AuthResponse;
import com.grupo8.turnos_app.auth.dto.LoginRequest;
import com.grupo8.turnos_app.auth.dto.RegisterRequest;
import com.grupo8.turnos_app.modules.users.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/register/client")
    public ResponseEntity<AuthResponse> registerClient(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClient(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getMe(userDetails.getUsername()));
    }
}