package com.grupo8.turnos_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.common.enums.BusinessTypeName;
import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.modules.business.entities.BusinessType;
import com.grupo8.turnos_app.modules.business.repositories.BusinessTypeRepository;
import com.grupo8.turnos_app.modules.role.entity.Role;
import com.grupo8.turnos_app.modules.role.repository.RoleRepository;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Override
    public void run(String... args) {
        seedRoles();
        seedBusinessTypes();
        seedAdmin();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("[Seeder] Role created: {}", roleName);
            }
        }
        log.info("[Seeder] Roles OK");
    }

    private void seedBusinessTypes() {
        for (BusinessTypeName typeName : BusinessTypeName.values()) {
            if (!businessTypeRepository.existsByName(typeName.name())) {
                businessTypeRepository.save(
                        BusinessType.builder()
                                .name(typeName.name())
                                .build());
                log.info("[Seeder] BusinessType created: {}", typeName);
            }
        }
        log.info("[Seeder] BusinessTypes OK");
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("[Seeder] Admin already exists, skipping");
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "[Seeder] ADMIN role not found — seedRoles() must run first"));

        User admin = User.builder()
                .name(adminName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .build();

        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        log.info("[Seeder] Admin created: {}", adminEmail);
    }
}