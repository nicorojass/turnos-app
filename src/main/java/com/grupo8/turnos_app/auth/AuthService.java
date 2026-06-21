package com.grupo8.turnos_app.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.auth.dto.AuthResponse;
import com.grupo8.turnos_app.auth.dto.LoginRequest;
import com.grupo8.turnos_app.auth.dto.RegisterRequest;
import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.common.exception.EmailAlreadyInUseException;
import com.grupo8.turnos_app.common.exception.ForbiddenOperationException;
import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.role.entity.Role;
import com.grupo8.turnos_app.modules.role.repository.RoleRepository;
import com.grupo8.turnos_app.modules.users.dto.UserResponse;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.mapper.UserMapper;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndActiveTrue(request.getEmail()))
            throw new EmailAlreadyInUseException("This email address is already registered. Please use a different one.");

        Role ownerRole = roleRepository.findByName(RoleName.OWNER)
                .orElseThrow(() -> new NotFoundException("A required system role could not be found. Please contact support."));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user.getRoles().add(ownerRole);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("No account was found with that email address."));

        if (!Boolean.TRUE.equals(user.getActive())){
        throw new ForbiddenOperationException("Your account has been deactivated. Please contact support for assistance.");
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();
    }

    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No account was found with that email address."));
        return UserMapper.toResponse(user);
    }

    public AuthResponse registerClient(RegisterRequest request) {
    if (userRepository.existsByEmailAndActiveTrue(request.getEmail()))
        throw new EmailAlreadyInUseException("This email address is already registered. Please use a different one.");

    Role clientRole = roleRepository.findByName(RoleName.CLIENT)
            .orElseThrow(() -> new NotFoundException("A required system role could not be found. Please contact support."));

    User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

    user.getRoles().add(clientRole);
    userRepository.save(user);

    String token = jwtService.generateToken(user);
    return AuthResponse.builder().token(token).build();
}
}
