package com.grupo8.turnos_app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.common.enums.BusinessTypeName;
import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.modules.business.entities.BusinessType;
import com.grupo8.turnos_app.modules.business.repositories.BusinessTypeRepository;
import com.grupo8.turnos_app.modules.role.entity.Role;
import com.grupo8.turnos_app.modules.role.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

// uncomment @component to enable dataseeder when all remaining was implemented
// @Component
@RequiredArgsConstructor
@Slf4j // console logger to check this dataseeder execution 
public class DataSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final BusinessTypeRepository businessTypeRepository;
  // (PARA NICO) (PARA NICO) 
  // uncomment when user is finished
  // this is for creating an admin user when running the project

  // private final UserRepository userRepository;
  // private final PasswordEncoder passwordEncoder; // for encoding admin password
  // before saving it into db

  // add the following values to application.properties (when testing) and
  // uncomment @value attributes
  /*
   * app.admin.email=admin@turnosapp.com
   * app.admin.password=Admin1234!
   * app.admin.name=Admin
   * 
   * @Value("${app.admin.email}")
   * private String adminEmail;
   * 
   * @Value("${app.admin.password}")
   * private String adminPassword;
   * 
   * @Value("${app.admin.name}")
   * private String adminName;
   */

  @Override
  public void run(String... args) {

    seedRoles();
    seedBusinessTypes();
    //seedAdmin();
  }

  // ----------------------------------------------------------------
  // 1. Roles seed
  // ----------------------------------------------------------------

  private void seedRoles() {
    for (RoleName roleName : RoleName.values()) {
      if (!roleRepository.existsByName(roleName)) {
        roleRepository.save(Role.builder().name(roleName).build());
        log.info("[Seeder] Role created: {}", roleName);
      }
    }

    log.info("[Seeder] Roles OK");
  }

  // 2. Business types seed

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

  // 3. Admin seed
  // run after seedRoles() to use them in this function

  /*private void seedAdmin() {
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
  }*/
}