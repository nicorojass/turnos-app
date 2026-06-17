package com.grupo8.turnos_app.modules.business.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.modules.business.entities.BusinessType;


public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {
    boolean existsByName(String name);
    List<BusinessType> findAllByDeletedFalse();
    Optional<BusinessType> findByPublicId(UUID publicId);
}