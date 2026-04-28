package com.grupo8.turnos_app.modules.service.mapper;

import com.grupo8.turnos_app.modules.service.Serv;
import com.grupo8.turnos_app.modules.service.dto.ServRequest;
import com.grupo8.turnos_app.modules.service.dto.ServResponse;


public class ServMapper {

    public static ServResponse toResponse(Serv service) {
        if (service == null) {
            return null;
        }
        return ServResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice().doubleValue())
                .businessId(service.getBusiness().getId())
                .build();
    }

    public static Serv toEntity(ServRequest serviceRequest) {
        return Serv.builder()
                .name(serviceRequest.getName())
                .durationMinutes(serviceRequest.getDurationMinutes())
                .price(serviceRequest.getPrice())
                .build();
    }
}
