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
            throw new BusinessAlreadyExistsException("This email is already registered to another business. Please use a different one.");
        if (businessRepository.existsBySlug(request.getSlug()))
            throw new BusinessAlreadyExistsException("That custom link is already taken. Please choose a different one.");
        if (businessRepository.existsByPhone(request.getPhone()))
            throw new BusinessAlreadyExistsException("This phone number is already registered to another business. Please use a different one.");

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException("We couldn't create your business because your account could not be found. Please try again or contact support."));

        if (businessRepository.findByOwnerId(owner.getId()).isPresent())
            throw new BusinessAlreadyExistsException("You already have a business registered with this account. Only one business per account is allowed.");

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
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));
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
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));
    }

    public BusinessResponse updateBusiness(UUID publicId, BusinessRequest request) {
        Business business = businessRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));

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
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));

        if (appointmentRepository.hasPendingAppointmentsByBusiness(business.getId(), LocalDateTime.now()))
            throw new PendingAppointmentsException("This business can't be deleted because it has upcoming appointments that are booked or awaiting payment.");

        business.setDeleted(true);
        businessRepository.save(business);
    }

    public void forceDeleteBusiness(UUID publicId) {
        Business business = businessRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));
                if (appointmentRepository.hasPendingAppointmentsByBusiness(business.getId(), LocalDateTime.now())) {
        throw new PendingAppointmentsException("This business can't be deleted because it has upcoming appointments that are booked or awaiting payment.");
    }
        businessRepository.delete(business);
    }

    public BusinessResponse addTypeToBusiness(UUID businessId, UUID typeId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));

        BusinessType businessType = businessTypeRepository.findByPublicId(typeId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("The selected business category could not be found."));

        if (business.getBusinessTypes().contains(businessType))
            throw new BusinessTypeAlreadyAssignedException("This business already has that category assigned.");

        business.getBusinessTypes().add(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse removeTypeFromBusiness(UUID businessId, UUID typeId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));

        BusinessType businessType = businessTypeRepository.findByPublicId(typeId)
                .orElseThrow(() -> new BusinessTypeNotFoundException("The selected business category could not be found."));

        business.getBusinessTypes().remove(businessType);
        return BusinessMapper.toResponse(businessRepository.save(business));
    }

    public BusinessResponse getMyBusiness(String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new OwnerNotFoundException("Your account could not be found. Please try again or contact support."));
        return businessRepository.findByOwner_Id(owner.getId())
                .map(BusinessMapper::toResponse)
                .orElseThrow(() -> new BusinessNotFoundException("We couldn't find the business you're looking for."));
    }

}
