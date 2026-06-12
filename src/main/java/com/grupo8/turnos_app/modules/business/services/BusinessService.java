package com.grupo8.turnos_app.modules.business.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessResponse;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.entities.BusinessType;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessAlreadyExistsException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessTypeAlreadyAssignedException;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessTypeNotFoundException;
import com.grupo8.turnos_app.modules.business.exceptions.OwnerNotFoundException;
import com.grupo8.turnos_app.modules.business.mapper.BusinessMapper;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.business.repositories.BusinessTypeRepository;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;
import com.grupo8.turnos_app.modules.day_schedule.service.DayScheduleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final DayScheduleService dayScheduleService;

    public BusinessResponse createBusiness(String ownerEmail, BusinessRequest request) {

    if (businessRepository.existsByEmail(request.getEmail()))
        throw new BusinessAlreadyExistsException("A business with that email already exists");
    if (businessRepository.existsBySlug(request.getSlug()))
        throw new BusinessAlreadyExistsException("A business with that slug already exists");
    if (businessRepository.existsByPhone(request.getPhone()))
        throw new BusinessAlreadyExistsException("A business with that phone already exists");

    User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new OwnerNotFoundException("Owner not found"));

    if (businessRepository.findByOwnerId(owner.getId()).isPresent())
        throw new BusinessAlreadyExistsException("This owner already has a business");

    Business business = BusinessMapper.toEntity(request);
    business.setOwner(owner);

    if (request.getTypeIds() != null && !request.getTypeIds().isEmpty()) {
        List<BusinessType> types = businessTypeRepository.findAllById(request.getTypeIds());
        business.setBusinessTypes(types);
    }

    Business savedBusiness = businessRepository.save(business);
    dayScheduleService.initializeScheduleForBusiness(savedBusiness);

    return BusinessMapper.toResponse(savedBusiness);
}

    public BusinessResponse getBusinessById(Long id) {
        return businessRepository.findById(id)
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
    }

    public List<BusinessResponse> getAllBusinesses() {
        return businessRepository.findAll()
                .stream()
                .map(BusinessMapper::toResponse)
                .toList();
    }

    public BusinessResponse getBusinessBySlug(String slug) {
        return businessRepository.findBySlug(slug)
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
    }

    public BusinessResponse updateBusiness(Long id, BusinessRequest request) {
        Business business = businessRepository.findById(id)
            .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new OwnerNotFoundException("Owner not found"));

        business.setName(request.getName());
        business.setEmail(request.getEmail());
        business.setSlug(request.getSlug());
        business.setPhone(request.getPhone());
        business.setDescription(request.getDescription());
        business.setAutomaticSchedule(request.getAutomaticSchedule());
        business.setScheduleEnd(request.getScheduleEnd());
        business.setScheduleDaysToCreate(request.getScheduleDaysToCreate());
        business.setScheduleAnticipation(request.getScheduleAnticipation());
        business.setOwner(owner);

        if (request.getTypeIds() != null) {
        List<BusinessType> types = businessTypeRepository.findAllById(request.getTypeIds());
        business.setBusinessTypes(types);
        }
        
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public void deleteBusiness(Long id) {
        Business business = businessRepository.findById(id)
            .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
        business.setActive(false);
        businessRepository.save(business);
    }

    public void forceDeleteBusiness(Long id) {
        if (!businessRepository.existsById(id))
            throw new BusinessNotFoundException("Business not found");
        businessRepository.deleteById(id);
    }

    public BusinessResponse addTypeToBusiness(Long businessId, Long typeId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        BusinessType businessType = businessTypeRepository.findById(typeId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Business type not found"));

        if (business.getBusinessTypes().contains(businessType))
            throw new BusinessTypeAlreadyAssignedException("Business already has this type");

        business.getBusinessTypes().add(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse removeTypeFromBusiness(Long businessId, Long typeId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        BusinessType businessType = businessTypeRepository.findById(typeId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Business type not found"));

        business.getBusinessTypes().remove(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse getMyBusiness(String email) {
        User owner = userRepository.findByEmail(email)
            .orElseThrow(() -> new OwnerNotFoundException("Owner not found"));
        return businessRepository.findByOwner_Id(owner.getId())
            .map(BusinessMapper::toResponse)
            .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
}

}
