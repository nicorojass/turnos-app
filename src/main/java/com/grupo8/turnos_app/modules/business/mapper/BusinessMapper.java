package com.grupo8.turnos_app.modules.business.mapper;

import java.util.Collections;
import java.util.stream.Collectors;

import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessResponse;
import com.grupo8.turnos_app.modules.business.entities.Business;

public class BusinessMapper {

    public static BusinessResponse toResponse(Business business) {
        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .email(business.getEmail())
                .slug(business.getSlug())
                .phone(business.getPhone())
                .description(business.getDescription())
                .automaticSchedule(business.getAutomaticSchedule())
                .scheduleEnd(business.getScheduleEnd())
                .scheduleDaysToCreate(business.getScheduleDaysToCreate())
                .scheduleAnticipation(business.getScheduleAnticipation())
                .active(business.getActive())
                .ownerId(business.getOwner().getId())
                .businessTypes(
                    business.getBusinessTypes() == null ? Collections.emptyList() :
                    business.getBusinessTypes().stream()
                        .map(BusinessTypeMapper::toResponse)
                        .collect(Collectors.toList())
                )
                .build();
    }

    public static Business toEntity(BusinessRequest request) {
        return Business.builder()
                .name(request.getName())
                .email(request.getEmail())
                .slug(request.getSlug())
                .phone(request.getPhone())
                .description(request.getDescription())
                .automaticSchedule(request.getAutomaticSchedule())
                .scheduleEnd(request.getScheduleEnd())
                .scheduleDaysToCreate(request.getScheduleDaysToCreate())
                .scheduleAnticipation(request.getScheduleAnticipation())
                .build();
    }
}