package com.grupo8.turnos_app.modules.business.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessResponse;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.entities.BusinessType;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.mapper.BusinessMapper;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.business.repositories.BusinessTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {
    
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final BusinessTypeRepository businessTypeRepository;

    public BusinessResponse createBusiness(BusinessRequest request) {
        
        if (businessRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("A business with that email already exists");
        if (businessRepository.existsBySlug(request.getSlug()))
            throw new RuntimeException("A business with that slug already exists");
        if (businessRepository.existsByPhone(request.getPhone()))
            throw new RuntimeException("A business with that phone already exists");

        User owner = userRepository.findById(request.getOwnerId())
<<<<<<< Updated upstream
            .orElseThrow(() -> new RuntimeException("Owner not found"));
=======
                .orElseThrow(() -> new BusinessNotFoundException("Owner not found"));
>>>>>>> Stashed changes

        Business business = BusinessMapper.toEntity(request);
        business.setOwner(owner);

        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse getBusinessById(Long id) {
        return businessRepository.findById(id)
<<<<<<< Updated upstream
            .map(BusinessMapper::toResponse)
            .orElseThrow(() -> new RuntimeException("Business not found"));
=======
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
>>>>>>> Stashed changes
    }

    public List<BusinessResponse> getAllBusinesses() {
        return businessRepository.findAll()
            .stream()
            .map(BusinessMapper::toResponse)
            .toList();
    }

    public BusinessResponse getBusinessBySlug(String slug) {
        return businessRepository.findBySlug(slug)
<<<<<<< Updated upstream
            .map(BusinessMapper::toResponse)
            .orElseThrow(() -> new RuntimeException("Business not found"));
=======
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
>>>>>>> Stashed changes
    }

    public BusinessResponse updateBusiness(Long id, BusinessRequest request) {
        Business business = businessRepository.findById(id)
<<<<<<< Updated upstream
            .orElseThrow(() -> new RuntimeException("Business not found"));

        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new RuntimeException("Owner not found"));
=======
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new BusinessNotFoundException("Owner not found"));
>>>>>>> Stashed changes

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

        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public void deleteBusiness(Long id) {
        Business business = businessRepository.findById(id)
<<<<<<< Updated upstream
            .orElseThrow(() -> new RuntimeException("Business not found"));
=======
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
            .orElseThrow(() -> new RuntimeException("Business not found"));
=======
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
>>>>>>> Stashed changes

        BusinessType businessType = businessTypeRepository.findById(typeId)
            .orElseThrow(() -> new RuntimeException("Business type not found"));

        if (business.getBusinessTypes().contains(businessType))
            throw new RuntimeException("Business already has this type");

        business.getBusinessTypes().add(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse removeTypeFromBusiness(Long businessId, Long typeId) {
        Business business = businessRepository.findById(businessId)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        BusinessType businessType = businessTypeRepository.findById(typeId)
            .orElseThrow(() -> new RuntimeException("Business type not found"));

        business.getBusinessTypes().remove(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

}
