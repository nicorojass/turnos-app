package com.grupo8.turnos_app.modules.business.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.modules.business.entities.Business;


public interface BusinessRepository extends JpaRepository<Business, Long> {
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
    boolean existsByPhone(String phone);
    Optional<Business> findBySlug(String slug);
    Optional<Business> findByOwner_Id(Long ownerId);
    List<Business> findByEmployees_Id(Long userId);
    Optional<Business> findByOwnerId(Long ownerId);
    Optional<Business> findByPublicId(UUID publicId);
    List<Business> findAllByAutomaticScheduleTrueAndDeletedFalse();
}