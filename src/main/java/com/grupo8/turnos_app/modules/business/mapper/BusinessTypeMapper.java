package com.grupo8.turnos_app.modules.business.mapper;

import com.grupo8.turnos_app.modules.business.dto.BusinessTypeRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessTypeResponse;
import com.grupo8.turnos_app.modules.business.entities.BusinessType;

public class BusinessTypeMapper {

    public static BusinessTypeResponse toResponse(BusinessType businessType) {
        return BusinessTypeResponse.builder()
                .id(businessType.getId())
                .name(businessType.getName())
                .active(businessType.getActive())
                .build();
    }

    public static BusinessType toEntity(BusinessTypeRequest request) {
        return BusinessType.builder()
                .name(request.getName())
                .build();
    }
}