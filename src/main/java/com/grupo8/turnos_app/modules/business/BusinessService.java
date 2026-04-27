package com.grupo8.turnos_app.modules.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessResponse;
import com.grupo8.turnos_app.modules.business.mapper.BusinessMapper;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class BusinessService {
    
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public BusinessResponse createBusiness(BusinessRequest request) {
        
        if(!businessRepository.existsByEmailAndSlugAndPhone(request.getEmail(), request.getSlug(), request.getPhone())) {
            throw new RuntimeException("Business with the same email, slug or phone already exists");
        }

        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new RuntimeException("Owner not found"));

        Business business = BusinessMapper.toEntity(request);
        business.setOwner(owner);

        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse getBusinessById(Long id) {
        return businessRepository.findById(id)
            .map(BusinessMapper::toResponse)
            .orElseThrow(() -> new RuntimeException("Business not found"));
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
            .orElseThrow(() -> new RuntimeException("Business not found"));
    }

    public BusinessResponse updateBusiness(Long id, BusinessRequest request) {
        Business business = businessRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        User owner = userRepository.findById(request.getOwnerId())
            .orElseThrow(() -> new RuntimeException("Owner not found"));

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
        if(!businessRepository.existsById(id)) {
            throw new RuntimeException("Business not found");
        }
        businessRepository.deleteById(id);
    }

}
