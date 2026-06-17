package com.grupo8.turnos_app.modules.service.service;

import java.util.List;
import java.util.UUID;

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

    public ServResponse createService(UUID businessId, ServRequest request) {

    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

    Serv service = ServMapper.toEntity(request);
    service.setBusiness(business);
    service.setDeleted(false);

    if (serviceRepository.existsByBusinessIdAndNameAndDurationMinutesAndPriceAndDepositPorcentage(
        business.getId(), request.getName(), request.getDurationMinutes(), request.getPrice(), request.getDepositPorcentage())) {
    throw new ServiceAlreadyExistsException("An identical service already exists for this business");
}
    return ServMapper.toResponse(serviceRepository.save(service));
}

    public List<ServResponse> getServicesByBusinessId(UUID businessId) {
        Business business = businessRepository.findByPublicId(businessId)
            .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
        List<Serv> services = serviceRepository.findByBusinessId(business.getId());
        return services.stream()
                .map(ServMapper::toResponse)
                .toList();
    }

    public ServResponse editService(UUID publicId, ServRequest request) {
    Serv service = serviceRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Service not found"));

    if (serviceRepository.existsByBusinessIdAndNameAndDurationMinutesAndPriceAndDepositPorcentage(
            service.getBusiness().getId(), request.getName(), request.getDurationMinutes(),
            request.getPrice(), request.getDepositPorcentage())) {
        throw new ServiceAlreadyExistsException("An identical service already exists for this business");
    }

    service.setName(request.getName());
    service.setPrice(request.getPrice());
    service.setDurationMinutes(request.getDurationMinutes());
    service.setDepositPorcentage(request.getDepositPorcentage());

    return ServMapper.toResponse(serviceRepository.save(service));
}


    public void deleteService(UUID publicId) {
        Serv service = serviceRepository.findByPublicId(publicId).orElseThrow(() -> new NotFoundException("Service not found"));
        service.setDeleted(true);
        serviceRepository.save(service);
    }
}