package com.grupo8.turnos_app.modules.service.mapper;

import com.grupo8.turnos_app.modules.service.Service;
import com.grupo8.turnos_app.modules.service.dto.ServiceResponse;

public class ServiceToDTO {

    public static ServiceResponse toDTO(Service service) {
        if (service == null) {
            return null;
        }
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice().doubleValue())
                .businessId(service.getBusiness().getId())
                .build();
    }    
}

    
