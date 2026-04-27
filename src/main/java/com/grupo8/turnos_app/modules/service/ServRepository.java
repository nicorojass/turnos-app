package com.grupo8.turnos_app.modules.service;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServRepository extends JpaRepository<Serv, Long> {
    
    List<Serv> findByBusinessId(Long businessId);
    
}
