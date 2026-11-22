package com.grupo8.turnos_app.modules.role.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.modules.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
    boolean existsByName(RoleName name);
    Optional<Role> findByPublicId(UUID publicId);
}
