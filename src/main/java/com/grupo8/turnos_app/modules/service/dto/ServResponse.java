package com.grupo8.turnos_app.modules.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServResponse {
    
    private Long id;
    private String name;
    private Integer durationMinutes;
    private Double price;
    private Long businessId;

}
