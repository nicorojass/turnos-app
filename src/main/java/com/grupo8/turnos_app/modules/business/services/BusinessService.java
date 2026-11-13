package com.grupo8.turnos_app.modules.business.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.security.BusinessAccessGuard;
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
    private final BusinessAccessGuard businessAccessGuard;
    private final UserRepository userRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final DayScheduleService dayScheduleService;
    private final AgendaGeneratorService agendaGeneratorService;
    private final AppointmentRepository appointmentRepository;
    
    // first segments of the front routes: a business with one of these as its link could never be opened
    public static final Set<String> RESERVED_SLUGS = Set.of("panel", "admin", "ingresar", "reserva", "mis-turnos");

    private void checkSlugNotReserved(String slug) {
        if (slug != null && RESERVED_SLUGS.contains(slug))
            throw new BusinessAlreadyExistsException("Ese link está reservado por la aplicación. Elegí otro");
    }

    public BusinessResponse createBusiness(String ownerEmail, BusinessRequest request) {
        checkSlugNotReserved(request.getSlug());

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
            List<BusinessType> types = findTypes(request.getTypeIds());
            business.setBusinessTypes(types);
        }

        Business savedBusiness = businessRepository.save(business);
        dayScheduleService.initializeScheduleForBusiness(savedBusiness);

        return BusinessMapper.toResponse(savedBusiness);
    }

    // findAllById silently skipped the ids that don't exist, so the front thought it saved a type
    // that wasn't saved. deleted types can't be assigned either
    private List<BusinessType> findTypes(List<Long> typeIds) {
        List<BusinessType> types = businessTypeRepository.findAllById(typeIds);
        long requested = typeIds.stream().distinct().count();
        if (types.size() != requested || types.stream().anyMatch(type -> Boolean.TRUE.equals(type.getDeleted())))
            throw new BusinessTypeNotFoundException("Rubro no encontrado");
        return types;
    }

    public BusinessResponse getBusinessById(UUID publicId) {
        return BusinessMapper.toResponse(businessAccessGuard.checkCanManage(publicId));
    }

    public List<BusinessResponse> getAllBusinesses() {
        return businessRepository.findAll()
                .stream()
                .map(BusinessMapper::toResponse)
                .toList();
    }

    public BusinessResponse getBusinessBySlug(String slug) {
        // a deleted business is not public anymore
        return businessRepository.findBySlug(slug)
                .filter(found -> !Boolean.TRUE.equals(found.getDeleted()))
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
    }

    public BusinessResponse updateBusiness(UUID publicId, BusinessRequest request) {
        Business business = businessRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
        businessAccessGuard.checkCanManage(business);
        checkSlugNotReserved(request.getSlug());

        // same checks as on create, skipping this business. before, a taken slug or email hit the
        // unique constraint (500) and a taken phone was saved
        if (businessRepository.existsByEmailAndIdNot(request.getEmail(), business.getId()))
            throw new BusinessAlreadyExistsException("Ya existe un negocio con ese email");
        if (businessRepository.existsBySlugAndIdNot(request.getSlug(), business.getId()))
            throw new BusinessAlreadyExistsException("El link personalizado ya existe. Ingresa uno distinto");
        if (businessRepository.existsByPhoneAndIdNot(request.getPhone(), business.getId()))
            throw new BusinessAlreadyExistsException("Ya existe un negocio con ese teléfono");

        // the agenda gets rebuilt from today (like sacaturno) when the automatic agenda is turned on,
        // or when it was already on and the owner changed how many days to create, for example from
        // 7 to 30 days. changing only the anticipation doesn't need it: that just moves when the cron
        // renews. turning it off keeps the free slots already published, sacaturno does the same.
        // agenda fields left out of the request keep their current value, so a screen that doesn't
        // edit the agenda can't turn it off or blank its settings by omission
        Boolean automaticSchedule = request.getAutomaticSchedule() != null
                ? request.getAutomaticSchedule() : business.getAutomaticSchedule();
        Integer scheduleDaysToCreate = request.getScheduleDaysToCreate() != null
                ? request.getScheduleDaysToCreate() : business.getScheduleDaysToCreate();

        boolean activatingSchedule = !Boolean.TRUE.equals(business.getAutomaticSchedule())
                && Boolean.TRUE.equals(automaticSchedule);
        boolean daysToCreateChanged = Boolean.TRUE.equals(automaticSchedule)
                && !Objects.equals(business.getScheduleDaysToCreate(), scheduleDaysToCreate);

        business.setName(request.getName());
        business.setEmail(request.getEmail());
        business.setSlug(request.getSlug());
        business.setPhone(request.getPhone());
        business.setDescription(request.getDescription());
        business.setAutomaticSchedule(automaticSchedule);
        // when it regenerates, regenerate sets scheduleEnd itself
        if (request.getScheduleEnd() != null)
            business.setScheduleEnd(request.getScheduleEnd());
        business.setScheduleDaysToCreate(scheduleDaysToCreate);
        if (request.getScheduleAnticipation() != null)
            business.setScheduleAnticipation(request.getScheduleAnticipation());

        if (request.getTypeIds() != null) {
            List<BusinessType> types = findTypes(request.getTypeIds());
            business.setBusinessTypes(types);
        }

        Business saved = businessRepository.save(business);

        if (activatingSchedule || daysToCreateChanged) {
            agendaGeneratorService.regenerate(saved);
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
        businessAccessGuard.checkCanManage(business);

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
        businessAccessGuard.checkCanManage(business);

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
