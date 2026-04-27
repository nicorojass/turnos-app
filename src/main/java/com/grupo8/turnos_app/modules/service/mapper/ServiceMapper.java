package com.grupo8.turnos_app.modules.service.mapper;

import com.grupo8.turnos_app.modules.service.Service;
import com.grupo8.turnos_app.modules.service.dto.ServiceRequest;
import com.grupo8.turnos_app.modules.service.dto.ServiceResponse;


public class ServiceMapper {

    public static ServiceResponse toResponse(Service service) {
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

    public static Service toEntity(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            return null;
        }
        Service service = new Service();
        service.setName(serviceRequest.getName());
        service.setDurationMinutes(serviceRequest.getDurationMinutes());
        service.setPrice(serviceRequest.getPrice());
        service.setBusinessId(serviceRequest.getBusinessId());

        return service;
    }
}
