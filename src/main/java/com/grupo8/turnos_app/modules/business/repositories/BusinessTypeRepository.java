package com.grupo8.turnos_app.modules.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo8.turnos_app.modules.business.entities.BusinessType;

public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {
    
}
