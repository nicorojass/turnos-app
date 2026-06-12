package com.grupo8.turnos_app.modules.users.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.modules.users.entities.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}