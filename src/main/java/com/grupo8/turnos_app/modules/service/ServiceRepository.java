package com.grupo8.turnos_app.modules.service;

import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository {
    Service save(Service service);
    Service findById(Long id);
    void deleteById(Long id);
    Iterable<Service> findAll();
    Iterable<Service> findByBusinessId(Long businessId);

}
