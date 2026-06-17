package com.grupo8.turnos_app.modules.business.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.modules.business.dto.BusinessTypeRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessTypeResponse;
import com.grupo8.turnos_app.modules.business.entities.BusinessType;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessAlreadyExistsException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessTypeNotFoundException;
import com.grupo8.turnos_app.modules.business.mapper.BusinessTypeMapper;
import com.grupo8.turnos_app.modules.business.repositories.BusinessTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessTypeService {

    private final BusinessTypeRepository businessTypeRepository;

    public BusinessTypeResponse createBusinessType(BusinessTypeRequest request) {
        if (businessTypeRepository.existsByName(request.getName()))
            throw new BusinessAlreadyExistsException("A business type with that name already exists");

        BusinessType businessType = BusinessTypeMapper.toEntity(request);
        return BusinessTypeMapper.toResponse(businessTypeRepository.save(businessType));
    }

    public List<BusinessTypeResponse> getAllBusinessTypes() {
        return businessTypeRepository.findAllByDeletedFalse()
                .stream()
                .map(BusinessTypeMapper::toResponse)
                .toList();
    }

    public BusinessTypeResponse updateBusinessType(Long id, BusinessTypeRequest request) {
        BusinessType businessType = businessTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Business type not found"));

        businessType.setName(request.getName());
        return BusinessTypeMapper.toResponse(businessTypeRepository.save(businessType));
    }

    public void toggleBusinessTypeDeleted(Long id) {
        BusinessType businessType = businessTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Business type not found"));

        businessType.setDeleted(!businessType.getDeleted());
        businessTypeRepository.save(businessType);
    }
}