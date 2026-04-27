package com.grupo8.turnos_app.modules.business;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
    boolean existsByEmailAndSlugAndPhone(String email, String slug, String phone);
    Optional<Business> findBySlug(String slug);
}
