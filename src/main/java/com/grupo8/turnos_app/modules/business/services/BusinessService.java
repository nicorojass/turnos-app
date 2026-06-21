package com.grupo8.turnos_app.modules.business.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.modules.agenda.service.AgendaGeneratorService;
import com.grupo8.turnos_app.modules.appointment.exceptions.PendingAppointmentsException;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
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
import com.grupo8.turnos_app.modules.day_schedule.service.DayScheduleService;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final DayScheduleService dayScheduleService;
    private final AgendaGeneratorService agendaGeneratorService;
    private final AppointmentRepository appointmentRepository;
    
    public BusinessResponse createBusiness(String ownerEmail, BusinessRequest request) {

        if (businessRepository.existsByEmail(request.getEmail()))
            throw new BusinessAlreadyExistsException("Ya existe un negocio con ese email");
        if (businessRepository.existsBySlug(request.getSlug()))
            throw new BusinessAlreadyExistsException("El link personalizado ya existe. Ingresa uno distinto");
        if (businessRepository.existsByPhone(request.getPhone()))
            throw new BusinessAlreadyExistsException("Ya existe un negocio con ese teléfono");

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException("Error al crear negocio: dueño no encontrado"));

        if (businessRepository.findByOwnerId(owner.getId()).isPresent())
            throw new BusinessAlreadyExistsException("Ya tenes un negocio creado con este usuario");

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

    public BusinessResponse getBusinessById(UUID publicId) {
        return businessRepository.findByPublicId(publicId)
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
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
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
    }

    public BusinessResponse updateBusiness(UUID publicId, BusinessRequest request) {
        Business business = businessRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

        // if activating automatic schedule, reset schedule end to trigger regeneration
        // from today
        boolean activatingSchedule = !Boolean.TRUE.equals(business.getAutomaticSchedule())
                && Boolean.TRUE.equals(request.getAutomaticSchedule());

        business.setName(request.getName());
        business.setEmail(request.getEmail());
        business.setSlug(request.getSlug());
        business.setPhone(request.getPhone());
        business.setDescription(request.getDescription());
        business.setAutomaticSchedule(request.getAutomaticSchedule());
        business.setScheduleEnd(activatingSchedule ? null : request.getScheduleEnd());
        business.setScheduleDaysToCreate(request.getScheduleDaysToCreate());
        business.setScheduleAnticipation(request.getScheduleAnticipation());

        if (request.getTypeIds() != null) {
            List<BusinessType> types = businessTypeRepository.findAllById(request.getTypeIds());
            business.setBusinessTypes(types);
        }

        Business saved = businessRepository.save(business);

        if (activatingSchedule) {
            agendaGeneratorService.runAutomation(saved);
        }

        return BusinessMapper.toResponse(saved);
    }

    public void deleteBusiness(UUID publicId) {
        Business business = businessRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

        if (appointmentRepository.hasPendingAppointmentsByBusiness(business.getId(), LocalDateTime.now()))
            throw new PendingAppointmentsException("El negocio tiene turnos pendientes y no puede ser eliminado");

        for (User employee : business.getEmployees()) {
        employee.setActive(false);
        userRepository.save(employee);
        }
        
        business.setDeleted(true);
        businessRepository.save(business);
    }

    public void forceDeleteBusiness(UUID publicId) {
        Business business = businessRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
                if (appointmentRepository.hasPendingAppointmentsByBusiness(business.getId(), LocalDateTime.now())) {
        throw new PendingAppointmentsException("El negocio tiene turnos pendientes y no puede ser eliminado");
    }
        businessRepository.delete(business);
    }

    public BusinessResponse addTypeToBusiness(UUID businessId, UUID typeId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

        BusinessType businessType = businessTypeRepository.findByPublicId(typeId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Tipo de negocio no encontrado"));

        if (business.getBusinessTypes().contains(businessType))
            throw new BusinessTypeAlreadyAssignedException("El negocio ya tiene este rubro asignado");

        business.getBusinessTypes().add(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse removeTypeFromBusiness(UUID businessId, UUID typeId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

        BusinessType businessType = businessTypeRepository.findByPublicId(typeId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("Rubro no encontrado"));

        business.getBusinessTypes().remove(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse getMyBusiness(String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new OwnerNotFoundException("Dueño no encontrado"));
        return businessRepository.findByOwner_Id(owner.getId())
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
    }

}
