package com.grupo8.turnos_app.modules.business.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo8.turnos_app.modules.business.entities.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
    boolean existsByPhone(String phone);
    Optional<Business> findBySlug(String slug);
    Optional<Business> findByOwner_Id(Long ownerId);
    List<Business> findByEmployees_Id(Long userId);
    Optional<Business> findByOwnerId(Long ownerId);
}