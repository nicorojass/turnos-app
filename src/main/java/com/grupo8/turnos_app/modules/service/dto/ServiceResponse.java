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
public class ServiceResponse {
    
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Long businessId;

}
