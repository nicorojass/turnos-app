package com.grupo8.turnos_app.modules.business.mapper;

import java.util.Collections;
import java.util.stream.Collectors;

import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessResponse;
import com.grupo8.turnos_app.modules.business.entities.Business;

public class BusinessMapper {

    public static BusinessResponse toResponse(Business business) {
        return BusinessResponse.builder()
                .id(business.getPublicId())
                .name(business.getName())
                .email(business.getEmail())
                .slug(business.getSlug())
                .phone(business.getPhone())
                .description(business.getDescription())
                .automaticSchedule(business.getAutomaticSchedule())
                .scheduleEnd(business.getScheduleEnd())
                .scheduleDaysToCreate(business.getScheduleDaysToCreate())
                .scheduleAnticipation(business.getScheduleAnticipation())
                .deleted(business.getDeleted())
                .ownerId(business.getOwner().getPublicId())
                .businessTypes(
                    business.getBusinessTypes() == null ? Collections.emptyList() :
                    business.getBusinessTypes().stream()
                        .map(BusinessTypeMapper::toResponse)
                        .collect(Collectors.toList())
                )
                .build();
    }

    public static Business toEntity(BusinessRequest request) {
        Business business = Business.builder()
                .name(request.getName())
                .email(request.getEmail())
                .slug(request.getSlug())
                .phone(request.getPhone())
                .description(request.getDescription())
                .scheduleEnd(request.getScheduleEnd())
                .build();

        // the builder defaults (agenda off, 30 days, 5 days of anticipation) are only kept
        // when the request leaves these fields out; passing null to the builder would overwrite them
        if (request.getAutomaticSchedule() != null)
            business.setAutomaticSchedule(request.getAutomaticSchedule());
        if (request.getScheduleDaysToCreate() != null)
            business.setScheduleDaysToCreate(request.getScheduleDaysToCreate());
        if (request.getScheduleAnticipation() != null)
            business.setScheduleAnticipation(request.getScheduleAnticipation());

        return business;
    }
}