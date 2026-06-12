package com.grupo8.turnos_app.modules.service.repository;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo8.turnos_app.modules.service.entity.Serv;

@Repository
public interface ServRepository extends JpaRepository<Serv, Long> {
    
    List<Serv> findByBusinessId(Long businessId);
    boolean existsByBusinessIdAndNameAndDurationMinutesAndPriceAndDepositPorcentage(
    Long businessId, String name, Integer durationMinutes, BigDecimal price, BigDecimal depositPorcentage);
    
    boolean existsByBusinessIdAndNameAndDurationMinutesAndPriceAndDepositPorcentageAndIdNot(
    Long businessId, String name, Integer durationMinutes, BigDecimal price, BigDecimal depositPorcentage, Long id);
}
