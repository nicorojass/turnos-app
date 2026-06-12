package com.grupo8.turnos_app.modules.service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.service.dto.ServRequest;
import com.grupo8.turnos_app.modules.service.dto.ServResponse;
import com.grupo8.turnos_app.modules.service.entity.Serv;
import com.grupo8.turnos_app.modules.service.exception.ServiceAlreadyExistsException;
import com.grupo8.turnos_app.modules.service.mapper.ServMapper;
import com.grupo8.turnos_app.modules.service.repository.ServRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServService {
    
    private final ServRepository serviceRepository;
    private final BusinessRepository businessRepository;

    public ServResponse createService(Long businessId, ServRequest request) {

    Business business = businessRepository.findById(businessId)
        .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

    Serv service = ServMapper.toEntity(request);
    service.setBusiness(business);
    service.setDeleted(false);

    if (serviceRepository.existsByBusinessIdAndNameAndDurationMinutesAndPriceAndDepositPorcentage(
        businessId, request.getName(), request.getDurationMinutes(), request.getPrice(), request.getDepositPorcentage())) {
    throw new ServiceAlreadyExistsException("An identical service already exists for this business");
}
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
            .orElseThrow(() -> new NotFoundException("Service not found"));

        service.setName(request.getName());
        service.setPrice(request.getPrice());
        service.setDurationMinutes(request.getDurationMinutes());

        return ServMapper.toResponse(serviceRepository.save(service));
}

    public void deleteService(Long serviceId) {
        Serv service = serviceRepository.findById(serviceId).orElseThrow(() -> new NotFoundException("Service not found"));
        service.setDeleted(true);
        serviceRepository.save(service);
    }
}