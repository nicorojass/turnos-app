package com.grupo8.turnos_app.modules.business.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        Optional<BusinessType> existing = businessTypeRepository.findByName(request.getName());
        if (existing.isPresent()) {
            BusinessType sameName = existing.get();
            if (!Boolean.TRUE.equals(sameName.getDeleted()))
                throw new BusinessAlreadyExistsException("Ya existe un rubro con ese nombre");

            // deletion is logical, so the deleted type still holds the unique name: creating it
            // again brings the deleted one back instead of failing on the unique constraint
            sameName.setDeleted(false);
            return BusinessTypeMapper.toResponse(businessTypeRepository.save(sameName));
        }

        BusinessType businessType = BusinessTypeMapper.toEntity(request);
        return BusinessTypeMapper.toResponse(businessTypeRepository.save(businessType));
    }

    public List<BusinessTypeResponse> getAllBusinessTypes() {
        return businessTypeRepository.findAllByDeletedFalse()
                .stream()
                .map(BusinessTypeMapper::toResponse)
                .toList();
    }

    public BusinessTypeResponse updateBusinessType(UUID publicId, BusinessTypeRequest request) {
        BusinessType businessType = businessTypeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Rubro no encontrado"));

        Optional<BusinessType> sameName = businessTypeRepository.findByName(request.getName());
        if (sameName.isPresent() && !sameName.get().getId().equals(businessType.getId())) {
            if (Boolean.TRUE.equals(sameName.get().getDeleted()))
                throw new BusinessAlreadyExistsException(
                        "Ya existe un rubro eliminado con ese nombre. Crealo de nuevo para reactivarlo");
            throw new BusinessAlreadyExistsException("Ya existe un rubro con ese nombre");
        }

        businessType.setName(request.getName());
        return BusinessTypeMapper.toResponse(businessTypeRepository.save(businessType));
    }

    public void toggleBusinessTypeDeleted(UUID publicId) {
        BusinessType businessType = businessTypeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Rubro no encontrado"));

        businessType.setDeleted(!Boolean.TRUE.equals(businessType.getDeleted()));
        businessTypeRepository.save(businessType);
    }
}