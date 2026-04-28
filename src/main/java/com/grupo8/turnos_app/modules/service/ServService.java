package com.grupo8.turnos_app.modules.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.modules.business.Business;
import com.grupo8.turnos_app.modules.service.dto.ServRequest;
import com.grupo8.turnos_app.modules.service.dto.ServResponse;
import com.grupo8.turnos_app.modules.service.mapper.ServMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServService {
    
    private final ServRepository serviceRepository;
    private final BusinessRepository businessRepository;

    public ServResponse createService(ServRequest request){

        Business business = businessRepository.findById(request.getBusinessId())
            .orElseThrow(() -> new RuntimeException("Business not found"));

        Serv service = ServMapper.toEntity(request);
        service.setBusiness(business);

        return ServMapper.toResponse(serviceRepository.save(service));
    }

    public List<ServResponse> getServicesByBusinessId(Long businessId) {
        List<Serv> services = serviceRepository.findByBusinessId(businessId);
        return services.stream()
                .map(ServMapper::toResponse)
                .toList();
    }

    public ServResponse editService(Long serviceId, ServRequest request) {
        Serv service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found"));

        service.setName(request.getName());
        service.setPrice(request.getPrice());
        service.setDurationMinutes(request.getDurationMinutes());

        return ServMapper.toResponse(serviceRepository.save(service));
}

    public void deleteService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new RuntimeException("Service not found");
        }
        serviceRepository.deleteById(serviceId);
    }
}