package com.grupo8.turnos_app.modules.service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    Iterable<Service> findByBusinessId(Long businessId);

}
